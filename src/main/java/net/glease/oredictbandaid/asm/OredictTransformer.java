package net.glease.oredictbandaid.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static org.objectweb.asm.Opcodes.ASM5;

public class OredictTransformer implements IClassTransformer {
	private final Map<String, BiFunction<Integer, ClassVisitor, ClassVisitor>> transformers = new HashMap<>();

	{
		transformers.put("net.minecraftforge.oredict.OreDictionary", OreDictionaryVisitor::new);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		BiFunction<Integer, ClassVisitor, ClassVisitor> factory = transformers.get(name);
		if (factory != null) {
			LoadingPlugin.log.info("Transforming class {}", name);
			ClassReader cr = new ClassReader(basicClass);
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
			if (true) {
//			if (false) {
				try (PrintWriter pw = new PrintWriter(name + ".txt", "UTF-8")) {
					cr.accept(factory.apply(ASM5, new TraceClassVisitor(cw, pw)), ClassReader.SKIP_DEBUG);
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					LoadingPlugin.log.warn("Unable to dump debug output", e);
					cr.accept(factory.apply(ASM5, cw), ClassReader.SKIP_DEBUG);
				}
			} else {
				cr.accept(factory.apply(ASM5, cw), ClassReader.SKIP_DEBUG);
			}
			return cw.toByteArray();
		}
		return basicClass;
	}
}
