package net.devtech.jerraria.network;

import net.devtech.jerraria.util.Validate;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KnownHosts {

	private static final Path FILE = Path.of(System.getProperty("user.home"), ".jerraria-known-hosts");

	private static Map<URI, X509Certificate> read() throws IOException {
		var map = new HashMap<URI, X509Certificate>();

		if (!Files.exists(FILE)) {
			return map;
		}

		Files.lines(FILE, StandardCharsets.UTF_8).forEach(x -> {
			var split = x.split(":");

			if (split.length != 2) {
				return;
			}

			try {
				var uri = new URI(new String(Base64.getDecoder().decode(split[0].getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
				var cert = load(Base64.getDecoder().decode(split[1].getBytes(StandardCharsets.UTF_8)));

				map.put(uri, cert);
			} catch (Throwable concern) {
				concern.printStackTrace();
			}
		});

		return map;
	}

	private static X509Certificate load(byte[] bytes) throws CertificateException {
		return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(bytes));
	}

	private static void write(Map<URI, X509Certificate> map) throws IOException {
		var list = new ArrayList<String>();

		for (var entry : map.entrySet()) {
			var uri = Base64.getEncoder().encode(entry.getKey().toString().getBytes(StandardCharsets.UTF_8));
			byte[] cert;

			try {
				cert = Base64.getEncoder().encode(entry.getValue().getEncoded());
			} catch (CertificateEncodingException exception) {
				throw Validate.rethrow(exception);
			}

			list.add(new String(uri, StandardCharsets.UTF_8) + ":" + new String(cert, StandardCharsets.UTF_8));
		}

		Files.createDirectories(FILE.getParent());
		Files.write(FILE, list, StandardCharsets.UTF_8);
	}

	public static synchronized boolean verify(URI address, X509Certificate certificate) {
		try {
			var map = read();
			var old = map.get(address);

			if (certificate.equals(old)) {
				return true;
			} else if (old == null) {
				var allow = JOptionPane.showConfirmDialog(null, "Do you wish to allow this server? " + hash(certificate), "New server identity", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0;

				if (allow) {
					map.put(address, certificate);
					write(map);
					return true;
				} else {
					return false;
				}
			} else {
				Toolkit.getDefaultToolkit().beep();
				var allow = JOptionPane.showConfirmDialog(null, "Do you wish to allow this server? " + hash(certificate) + " was " + hash(old), "Server identity changed", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == 0;

				if (allow) {
					map.put(address, certificate);
					write(map);
					return true;
				} else {
					return false;
				}
			}
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open known hosts offsets", "Error", JOptionPane.ERROR_MESSAGE);
		}

		return true;
	}

	private static String hash(X509Certificate certificate) {
		try {
			var instance = MessageDigest.getInstance("SHA-256");
			instance.update(certificate.getEncoded());
			return new String(Base64.getEncoder().encode(instance.digest()), StandardCharsets.UTF_8);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			return "ERROR";
		}
	}
}
