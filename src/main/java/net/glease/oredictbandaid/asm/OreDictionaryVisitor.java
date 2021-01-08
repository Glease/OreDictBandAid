package net.glease.oredictbandaid.asm;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class OreDictionaryVisitor extends ClassVisitor {
	private static class GetOreIDVisitor extends MethodVisitor {
		Label jumpEnd;
		Label jumpStart = new Label();
		public GetOreIDVisitor(int api, MethodVisitor mv) {
			super(api, mv);
		}

		@Override
		public void visitJumpInsn(int opcode, Label label) {
			super.visitJumpInsn(opcode, label);
			LoadingPlugin.log.trace("Adding getOreID MONITERENTER");
			jumpEnd = label;
			mv.visitFieldInsn(GETSTATIC, "net/minecraftforge/oredict/OreDictionary", "emptyEntryCreationLock", "Ljava/lang/Object;");
			mv.visitInsn(MONITORENTER);
			mv.visitLabel(jumpStart);
		}

		@Override
		public void visitLabel(Label label) {
			if (label.equals(jumpEnd)) {
				LoadingPlugin.log.trace("Adding getOreID MONITEREXIT");
				// normal exit
				mv.visitFieldInsn(GETSTATIC, "net/minecraftforge/oredict/OreDictionary", "emptyEntryCreationLock", "Ljava/lang/Object;");
				mv.visitInsn(MONITOREXIT);
				// not normal
				mv.visitJumpInsn(GOTO, jumpEnd);
				Label catchBegin = new Label();
				mv.visitLabel(catchBegin);
				mv.visitFieldInsn(GETSTATIC, "net/minecraftforge/oredict/OreDictionary", "emptyEntryCreationLock", "Ljava/lang/Object;");
				mv.visitInsn(MONITOREXIT);
				mv.visitInsn(ATHROW);
				// try-catch block metadata
				mv.visitTryCatchBlock(jumpStart, catchBegin, catchBegin, null);
			}
			super.visitLabel(label);
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
		}
	}

	private static class ClassInitializerVisitor extends MethodVisitor{
		public ClassInitializerVisitor(int api, MethodVisitor mv) {
			super(api, mv);
		}

		@Override
		public void visitCode() {
			super.visitCode();
			LoadingPlugin.log.trace("Adding emptyEntryCreationLock initializer");
			mv.visitTypeInsn(NEW, "java/lang/Object");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
			mv.visitFieldInsn(PUTSTATIC, "net/minecraftforge/oredict/OreDictionary","emptyEntryCreationLock", "Ljava/lang/Object;");
		}
		@Override
		public void visitEnd() {
			super.visitEnd();
		}
	}

	public OreDictionaryVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	}

	@Override
	public void visitEnd() {
		LoadingPlugin.log.debug("Adding emptyEntryCreationLock");
		cv.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "emptyEntryCreationLock", "Ljava/lang/Object;", null, null);
		super.visitEnd();
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("getOreID") && desc.equals("(Ljava/lang/String;)I")) {
			LoadingPlugin.log.debug("Patching getOreID");
			return new GetOreIDVisitor(api, super.visitMethod(access, name, desc, signature, exceptions));
		} else if (name.equals("<clinit>")) {
			// some classes don't have clinit. not this one.
			LoadingPlugin.log.debug("Patching clinit");
			return new ClassInitializerVisitor(api, super.visitMethod(access, name, desc, signature, exceptions));
		}
		return super.visitMethod(access, name, desc, signature, exceptions);
	}
}
