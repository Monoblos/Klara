package org.de.htwg.klara.transformers;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Transformer extends ClassNode {
	protected ClassVisitor cv = null;
	protected String className = "";

	public Transformer(int api, ClassVisitor cv) {
		super(api);
		this.cv = cv;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.className = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public void visitEnd() {
		super.visitEnd();
		
		if (cv != null) {
			transform();
			accept(cv);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void transform() {
		for (MethodNode mn : (List<MethodNode>) methods) {
			if ("<clinit>".equals(mn.name)) {
				continue;
			}
			methodTransform(mn);
		}
	}
	
	protected void methodTransform(MethodNode mn) {
		
	}
	
	public String getClassName() {
		return className;
	}
}
