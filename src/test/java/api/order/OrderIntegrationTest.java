package api.order;

import api.Main;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Random;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = Main.class
)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext
public class OrderIntegrationTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final int NUM_OF_CRUD_OPS = 100;
	private final int MAX_ITEMS_PER_ORDER = 1000; // TODO: REMOVE?

	private Random rand = new Random();

	private String genRandOrderItemsAndQuantities() {
		int items = rand.nextInt(MAX_ITEMS_PER_ORDER - 1) + 1;

		StringBuilder strBuilder = new StringBuilder("{\"purchaserName\":\"TestBuyer\",\"itemIDs\":[");
		long[] quantities = new long[NUM_OF_CRUD_OPS];

		boolean containsVal = false;
		for (int i = 0; i < items; i++) {
			int itemID = rand.nextInt(NUM_OF_CRUD_OPS - 1) + 1;
			if (quantities[itemID - 1] == 0)
				strBuilder.append(containsVal ? "," : "").append(itemID);
			quantities[itemID - 1]++;
			containsVal = true;
		}

		strBuilder.append("],\"quantities\":[");
		boolean addedQuantity = false;
		for (int i = 0; i < quantities.length; i++)
			if (quantities[i] > 0) {
				strBuilder.append(addedQuantity ? "," : "").append(quantities[i]);
				addedQuantity = true;
			}
		strBuilder.append("]}");

		return strBuilder.toString();
	}

	private String[] generateRandomOrders() {
		String[] catalogEntries = new String[NUM_OF_CRUD_OPS];

		for (int i = 0; i < NUM_OF_CRUD_OPS; i++)
			catalogEntries[i] = genRandOrderItemsAndQuantities();

		return catalogEntries;
	}

	private MvcResult createOrder(String orderJSON) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.post("/api/orders/")
									  .content(orderJSON)
									  .contentType(APPLICATION_JSON)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	private MvcResult findOrderByID(int orderID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.get("/api/orders/" + orderID)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	private MvcResult updateOrder(int orderID, String newOrderJSON) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.put("/api/orders/" + orderID)
									  .content(newOrderJSON)
									  .contentType(APPLICATION_JSON)
									  .accept(APPLICATION_JSON)
		).andReturn();
	}

	private MvcResult deleteOrder(int orderID) throws Exception {
		return mockMvc.perform(
				MockMvcRequestBuilders.delete("/api/orders/" + orderID)
		).andReturn();
	}

	private void initCatalogDatabase() {
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			jdbcTemplate.update(
					"insert into catalog (item_name, brand, star_rating, price, quantity) values (?,?,?,?,?)",
					"Item" + i, "Brand" + i, ((i % 5) + 1), i, i
			);
		}
	}

	private void emptyCatalogDatabase() {
		jdbcTemplate.update("truncate table catalog");
	}

	@Test
	public void crudTest() {
		initCatalogDatabase();

		String[] orders = generateRandomOrders();

		// Create
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			try {
				result = createOrder(orders[i]);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				TestCase.fail("Exception occurred while creating order with ID: " + (i + 1));
			}
		}

		// Read
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			int orderID = i + 1;
			try {
				result = findOrderByID(orderID);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
				TestCase.assertEquals(orders[i], result.getResponse().getContentAsString());
			} catch (Exception e) {
				TestCase.fail("Exception occurred while reading order with ID: " + orderID);
			}
		}

		// Update
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) { // Reverses order of entries in the database
			MvcResult result;
			int orderID = i + 1;
			try {
				result = updateOrder(orderID, orders[NUM_OF_CRUD_OPS - orderID]);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				TestCase.fail("Exception occurred while updating order with ID: " + orderID);
			}

			MvcResult getUpdatedResult;
			try {
				getUpdatedResult = findOrderByID(orderID);
				TestCase.assertEquals(HttpStatus.OK.value(), getUpdatedResult.getResponse().getStatus());
				TestCase.assertEquals(orders[NUM_OF_CRUD_OPS - orderID], getUpdatedResult.getResponse()
																						 .getContentAsString());
			} catch (Exception e) {
				TestCase.fail("Exception occurred while reading updated order with ID: " + orderID);
			}
		}

		// Delete
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			MvcResult result;
			int orderID = i + 1;
			try {
				result = deleteOrder(orderID);
				TestCase.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				TestCase.fail("Exception occurred while deleting order with ID: " + orderID);
			}
		}

		emptyCatalogDatabase();
	}

	@Test
	public void outOfBoundsTest() {
		for (int i = 0; i < NUM_OF_CRUD_OPS; i++) {
			try {
				MvcResult result = createOrder(genRandOrderItemsAndQuantities());
				TestCase.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), result.getResponse().getStatus());
			} catch (Exception e) {
				TestCase.fail("Exception occurred while creating an order");
			}
		}
	}
}
