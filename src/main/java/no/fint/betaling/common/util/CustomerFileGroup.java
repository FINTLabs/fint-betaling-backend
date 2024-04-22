package no.fint.betaling.common.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.fint.betaling.model.CustomerGroup;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CustomerFileGroup {
    CustomerGroup foundCustomers;
    List<String> notFoundCustomers;

    public boolean hasCustomers(){
        return !foundCustomers.getCustomers().isEmpty() || !notFoundCustomers.isEmpty();
    }
}
