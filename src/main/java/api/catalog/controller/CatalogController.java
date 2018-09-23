package api.catalog.controller;

import api.catalog.dao.CatalogDAO;
import api.catalog.model.CatalogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles client-server interactions, and specifies what the client can request from the server. Since the catalog is
 * ready only, specific catalog requests are the only operation allowed.
 */
@RestController
@RequestMapping("/api")
public class CatalogController {

	@Autowired
	private CatalogDAO catalogDAO;

	/**
	 * Returns a catalog entry given an ID, if it exists. If there is no catalog entry associated with the given ID,
	 * a 404 Not Found response will be returned.
	 *
	 * @param id ID of catalog entry to search for
	 * @return 200 OK response with a Catalog entry in its body if it existed in the database, or a 404 Not Found
	 * response with no body otherwise
	 */
	@GetMapping("/catalog/{id}")
	public ResponseEntity<CatalogEntry> getCatalogEntryById(@PathVariable(value = "id") Long id) {
		CatalogEntry catalogEntry = catalogDAO.findById(id);

		if (catalogEntry == null)
			return ResponseEntity.notFound().build();
		return ResponseEntity.ok().body(catalogEntry);
	}
}
