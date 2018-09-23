package api.order.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "Orders")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	private String purchaserName;

	@Lob
	private Long[] itemIDs, quantities;

	public String getPurchaserName() {
		return purchaserName;
	}

	public void setPurchaserName(String purchaserName) {
		this.purchaserName = purchaserName;
	}

	public Long[] getItemIDs() {
		return itemIDs;
	}

	public void setItemIDs(Long[] itemIDs) {
		this.itemIDs = itemIDs;
	}

	public Long[] getQuantities() {
		return quantities;
	}

	public void setQuantities(Long[] quantities) {
		this.quantities = quantities;
	}
}
