import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class JDKShit {
	public static void main(String[] args) throws IOException {
		FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
		Path path = fs.getPath("modules", "java.desktop", "sun");
		Files.walk(path).filter(Files::isRegularFile).forEach(path1 -> {
			try {
				if(path1.toString().endsWith(".class")) {
					ClassReader reader = new ClassReader(Files.readAllBytes(path1));
					reader.accept(new ClassVisitor(Opcodes.ASM9) {
						@Override
						public MethodVisitor visitMethod(
							int access, String name, String descriptor, String signature, String[] exceptions) {
							return new MethodVisitor(Opcodes.ASM9) {
								@Override
								public void visitLdcInsn(Object value) {
									if(value.equals("sun.java2d.opengl")) {
										System.out.println(path1);
									}
								}
							};
						}
					}, 0);
				}
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
