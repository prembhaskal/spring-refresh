package example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {

    private final CashCardRepository cashCardRepository;

    public CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        Optional<CashCard> cashCardOptional  = findCashCard(requestedId, principal);
        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private Optional<CashCard> findCashCard(Long requestedId, Principal principal) {
        return Optional.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
    }

    @PostMapping
    private ResponseEntity<CashCard> createCashCard(@RequestBody CashCard newCashCard, UriComponentsBuilder ucb,
                                                    Principal principal) {
        CashCard cardWithOwner = new CashCard(null, newCashCard.amount(), principal.getName());
        CashCard savedCard = cashCardRepository.save(cardWithOwner);
        URI newCardLocation = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCard.id())
                .toUri();
        return ResponseEntity.created(newCardLocation).build();
    }

    @GetMapping
    private ResponseEntity<Iterable<CashCard>> getList(Pageable pageable, Principal principal) {
        Page<CashCard> page =  cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        Optional<CashCard> existOptional  = findCashCard(requestedId, principal);
        if (!existOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        CashCard existingCard = existOptional.get();

        CashCard updatedCard = new CashCard(existingCard.id(), cashCardUpdate.amount(), principal.getName());
        cashCardRepository.save(updatedCard);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (!cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            return ResponseEntity.notFound().build();
        }
        cashCardRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
