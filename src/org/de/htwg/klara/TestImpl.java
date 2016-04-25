package org.de.htwg.klara;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import org.de.htwg.klara.CustomClassLoader.FilterType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TestImpl {
	private static class PrintingClassVisitor extends ClassVisitor {
		public PrintingClassVisitor(int api) {
			super(api);
		}
		public PrintingClassVisitor(int api, ClassVisitor cw) {
			super(api, cw);
		}
		
		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			String[] classpath = name.split("/");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < classpath.length - 1; i++) {
				if (i != 0)
					sb.append(".");
				sb.append(classpath[i]);
			}
			System.out.println("package " + sb.toString() + ";");
			System.out.println();
			sb = new StringBuilder("");
			if ((access & Opcodes.ACC_PUBLIC) > 0)
				sb.append("public ");
			System.out.println(sb.toString() + "class " + classpath[classpath.length - 1] + " {");

			super.visit(version, access, name, signature, superName, interfaces);
		}
		
		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			StringBuilder sb = new StringBuilder("");
			if ((access & Opcodes.ACC_PUBLIC) > 0)
				sb.append("public ");
			else if ((access & Opcodes.ACC_PRIVATE) > 0)
				sb.append("private ");
			else if ((access & Opcodes.ACC_PROTECTED) > 0)
				sb.append("protected ");
			if ((access & Opcodes.ACC_STATIC) > 0)
				sb.append("static ");
			String returnVal = desc.split("\\)")[1];
			switch (returnVal) {
			case "V":
				returnVal = "void";
				break;
			case "I":
				returnVal = "int";

			default:
				returnVal.replaceFirst("L", "");
				returnVal.replaceFirst(";", "");
				returnVal.replace("/", ".");
				break;
			}
			String params = desc.split("\\)")[0] + ")";
			System.out.println("    " + sb.toString() + returnVal + " " + name + params + " {");
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
		
		@Override
		public void visitEnd() {
			System.out.println("}");
			super.visitEnd();
		}
	}
	
	public static void main(String[] args) throws Exception {
		CustomClassLoader x = new CustomClassLoader(true, FilterType.WHITELIST, new Pattern[] { Pattern.compile(".*htwg\\.klara.*") }, PrintingClassVisitor.class);
		Class<?> foo = x.loadClass("org.de.htwg.klara.Foo");
		Method main = foo.getMethod("main", (new String[0]).getClass());
		Object argArray[] = { new String[0] };
		main.invoke(null, argArray);
	}
}
