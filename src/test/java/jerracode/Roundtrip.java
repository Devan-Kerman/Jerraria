package jerracode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import net.devtech.jerraria.jerracode.JCIO;
import net.devtech.jerraria.jerracode.NativeJCType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Roundtrip {
	@Test
	public void intRoundTrip() throws IOException {
		this.roundTrip(output -> {
			JCIO.write(NativeJCType.INT, 10, output);
		}, input -> {
			int test = JCIO.read(NativeJCType.INT, input);
			Assertions.assertEquals(10, test);
		});
	}

	interface UnsafeConsumer<T> {
		void accept(T t) throws IOException;
	}

	public void roundTrip(UnsafeConsumer<DataOutput> output, UnsafeConsumer<DataInput> input) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		output.accept(new DataOutputStream(baos));
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		input.accept(new DataInputStream(bais));
	}
}
