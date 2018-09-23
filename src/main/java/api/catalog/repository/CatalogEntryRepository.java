package api.catalog.repository;

import api.catalog.model.CatalogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Handles database operations relating to CatalogEntries, and more specifically, the 'catalog' table in the database.
 */
public interface CatalogEntryRepository extends JpaRepository<CatalogEntry, Long> {
}
