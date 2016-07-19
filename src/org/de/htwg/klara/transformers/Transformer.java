package org.de.htwg.klara.transformers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.de.htwg.klara.transformers.events.IincInsnEvent;
import org.de.htwg.klara.transformers.events.ScopeReachedEvent;
import org.de.htwg.klara.transformers.events.TransformationEvent;
import org.de.htwg.klara.transformers.events.TransformationEventListener;
import org.de.htwg.klara.transformers.events.VarInsnEvent;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class Transformer extends ClassNode {
	protected ClassVisitor cv = null;
	protected String className = "";
	protected LineNumberNode currentLine = null;
	protected MethodNode currentMethod = null;
	Map<Integer, LocalVariableNode> currentScope = new HashMap<>();
	List<LocalVariableNode> futureVariables = new LinkedList<>();
	protected boolean addLineInfo = false;
	private boolean currentLinePrinted = false;
	
	protected List<TransformationEventListener> listeners = new LinkedList<>();

	public Transformer(int api, ClassVisitor cv) {
		super(api);
		this.cv = cv;
	}
	
	public void addListener(TransformationEventListener listener) {
		removeListener(listener);
		listeners.add(listener);
	}
	
	public boolean removeListener(TransformationEventListener listener) {
		return listeners.remove(listener);
	}
	
	protected void sendEvent(TransformationEvent e) {
		for (TransformationEventListener tel : listeners) {
			tel.handle(e);
		}
	}
	
	public void setAddLineInfo(boolean value) {
		addLineInfo = value;
	}
	
	public boolean getAddLineInfo() {
		return addLineInfo;
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
			if ("<clinit>".equals(mn.name) || "<init>".equals(mn.name)) {
				continue;
			}
			methodTransform(mn);
		}
	}
	
	@SuppressWarnings("unchecked")
	synchronized protected void methodTransform(MethodNode mn) {
		currentMethod = mn;
		currentScope.clear();
		futureVariables.clear();
		
		for (LocalVariableNode lvn : (List<LocalVariableNode>)mn.localVariables) {
			futureVariables.add(lvn);
		}
		
		InsnList insns = mn.instructions;
		Iterator<AbstractInsnNode> j = insns.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			if (in.getType() == AbstractInsnNode.VAR_INSN) {
				VarInsnNode varIn = (VarInsnNode)in;
				sendEvent(new VarInsnEvent(varIn));
			} else if (in.getType() == AbstractInsnNode.IINC_INSN) {
				IincInsnNode iincIn = (IincInsnNode)in;
				sendEvent(new IincInsnEvent(iincIn));
			} else if (in.getType() == AbstractInsnNode.LABEL) {
				LabelNode label = (LabelNode)in;
				LocalVariableNode tmpNode;
				Integer[] scopeKeys = currentScope.keySet().toArray(new Integer[0]);
				for (int i = 0; i < scopeKeys.length; ++i) {
					tmpNode = currentScope.get(scopeKeys[i]);
					if (label.equals(tmpNode.end)) {
						currentScope.remove(scopeKeys[i]);
					}
				}
				for (int i = 0; i < futureVariables.size(); ++i) {
					tmpNode = futureVariables.get(i);
					if (label.equals(tmpNode.start)) {
						currentScope.put(tmpNode.index, tmpNode);
						futureVariables.remove(tmpNode);
						--i;
						sendEvent(new ScopeReachedEvent(label, tmpNode));
					}
				}
			} else if (in.getType() == AbstractInsnNode.LINE) {
				LineNumberNode lnn = (LineNumberNode)in;
				if (addLineInfo && !currentLinePrinted) {
					printLine(currentLine, new InsnList());
				}
				currentLinePrinted = false;
				currentLine = lnn;
			}
		}
	}
	
	/**
	 * Inserts a print with the current location and any additional text in the {@code InsnList what} after the Node identified by where.
	 * @param where	The Node after which the print should be added
	 * @param what	A sequence of append calls to the StringBuilder on the stack
	 */
	public void printLine(AbstractInsnNode where, InsnList what) {
		InsnList il = new InsnList();
		il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
		il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
		il.add(new InsnNode(Opcodes.DUP));
		il.add(new LdcInsnNode(TransformUtils.formatLocation(getClassName(), currentLine != null ? currentLine.line : 0)));
		il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
		il.add(what);
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
		currentMethod.instructions.insert(where, il);
		currentLinePrinted = true;
	}
	
	public String getClassName() {
		return className;
	}
	
	public LocalVariableNode getVar(int index) {
		return currentScope.get(index);
	}
}
