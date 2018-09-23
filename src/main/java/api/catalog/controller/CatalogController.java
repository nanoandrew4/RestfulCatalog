package api.catalog.controller;

import api.catalog.dao.CatalogDAO;
import api.catalog.model.CatalogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

	@Autowired
	private CatalogDAO catalogDAO;

	@PostMapping("/catalog")
	public CatalogEntry createCatalogEntry(@Valid @RequestBody CatalogEntry catalogEntry) {
		return catalogDAO.save(catalogEntry);
	}

	@GetMapping("/catalog")
	public List<CatalogEntry> getAllCatalogEntries() {
		return catalogDAO.findAll();
	}

	@GetMapping("/catalog/{id}")
	public ResponseEntity<CatalogEntry> getCatalogEntryById(@PathVariable(value = "id") Long id) {
		CatalogEntry catalogEntry = catalogDAO.findById(id);

		if (catalogEntry == null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok().body(catalogEntry);
	}

	@PutMapping("/catalog/{id}")
	public ResponseEntity<CatalogEntry> updateCatalogEntry(@PathVariable(value = "id") Long id, @Valid @RequestBody
														   CatalogEntry newCatalogEntry) {
		CatalogEntry catalogEntry = catalogDAO.findById(id);

		if (catalogEntry == null)
			return ResponseEntity.notFound().build();

		catalogEntry.setItemName(newCatalogEntry.getItemName());
		catalogEntry.setBrand(newCatalogEntry.getBrand());
		catalogEntry.setPrice(newCatalogEntry.getPrice());
		catalogEntry.setQuantity(newCatalogEntry.getQuantity());
		catalogEntry.setStarRating(newCatalogEntry.getStarRating());

		CatalogEntry updatedEntry = catalogDAO.save(catalogEntry);
		return ResponseEntity.ok().body(updatedEntry);
	}

	@DeleteMapping("/catalog/{id}")
	public ResponseEntity<CatalogEntry> deleteCatalogEntry(@PathVariable(value = "id") Long id) {
		CatalogEntry catalogEntry = catalogDAO.findById(id);

		if (catalogEntry == null)
			return ResponseEntity.notFound().build();

		catalogDAO.deleteCatalogEntryById(id);
		return ResponseEntity.ok().build();
	}
}
