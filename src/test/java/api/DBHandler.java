package api;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This class handles direct database access, in order to set up the tables to execute a particular test.
 */
public class DBHandler {

	/**
	 * Populates the catalog table with placeholder values, so that orders can be created.
	 *
	 * @param numOfItems   Number of catalog entries to insert into the 'catalog' table
	 * @param jdbcTemplate JdbcTemplate to use to issue queries on the database
	 */
	public static void populateCatalogTable(int numOfItems, JdbcTemplate jdbcTemplate) {
		for (int i = 0; i < numOfItems; i++) {
			jdbcTemplate.update(
					"insert into catalog (item_name, brand, star_rating, price) values (?,?,?,?)",
					"Item" + i, "Brand" + i, ((i % 5) + 1), i
			);
		}
	}

	/**
	 * Drops and recreates the 'catalog' table.
	 *
	 * @param jdbcTemplate JdbcTemplate to use to issue queries on the database
	 */
	public static void clearCatalogTable(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.update("truncate table catalog");
	}

	/**
	 * Drops and recreates the 'orders' table.
	 *
	 * @param jdbcTemplate JdbcTemplate to use to issue queries on the database
	 */
	public static void clearOrdersTable(JdbcTemplate jdbcTemplate) {
		jdbcTemplate.update("truncate table orders");
	}
}
