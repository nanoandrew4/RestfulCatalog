package api.catalog.dao;

import api.catalog.model.CatalogEntry;
import api.catalog.repository.CatalogEntryRepository;
import jdk.internal.jline.internal.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Constrains server side data access, to limit what the controller is able to do.
 */
@Service
public class CatalogDAO {

	@Autowired
	private CatalogEntryRepository catalogEntryRepository;

	/**
	 * Not implemented in controller. API design requires that it be read only, but the database needs to be loaded
	 * somehow...
	 *
	 * @param catalogEntry A catalog entry
	 * @return The saved catalog entry
	 */
	public CatalogEntry save(CatalogEntry catalogEntry) {
		return catalogEntryRepository.save(catalogEntry);
	}

	/**
	 * Searches for a catalog entry given an ID. If it is found, it will return the catalog entry, otherwise it will
	 * return null.
	 *
	 * @param id ID of catalog entry to return
	 * @return The requested catalog entry, if it exists in the database, or null, if it does not exist
	 */
	@Nullable
	public CatalogEntry findById(Long id) {
		return catalogEntryRepository.findById(id).orElse(null);
	}
}
