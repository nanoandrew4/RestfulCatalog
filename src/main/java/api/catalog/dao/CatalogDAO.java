package api.catalog.dao;

import api.catalog.model.CatalogEntry;
import api.catalog.repository.CatalogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogDAO {

	@Autowired
	private CatalogEntryRepository catalogEntryRepository;

	public CatalogEntry save(CatalogEntry catalogEntry) {
		return catalogEntryRepository.save(catalogEntry);
	}

	public List<CatalogEntry> findAll() {
		return catalogEntryRepository.findAll();
	}

	public CatalogEntry findById(Long id) {
		return catalogEntryRepository.getOne(id);
	}

	public void deleteCatalogEntryById(Long id) {
		catalogEntryRepository.deleteById(id);
	}

	public long getNumOfElements() {
		return catalogEntryRepository.count();
	}
}
