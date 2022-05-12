package idpacking;

import net.devtech.jerraria.util.IdentifierPacker;
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
