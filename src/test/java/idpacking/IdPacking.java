package idpacking;

import net.devtech.jerraria.registry.IdentifierPacker;
import net.devtech.jerraria.util.Validate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdPacking {
	@Test
	public void idPacking() {
		String jerraria = "jerraria";
		long test = IdentifierPacker.pack(jerraria);
		Assertions.assertEquals(jerraria, IdentifierPacker.unpack(test));
	}
}
