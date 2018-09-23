package api.catalog.repository;

import api.catalog.model.CatalogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogEntryRepository extends JpaRepository<CatalogEntry, Long> {
}
