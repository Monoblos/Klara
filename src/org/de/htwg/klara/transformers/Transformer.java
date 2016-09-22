package org.de.htwg.klara.transformers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.de.htwg.klara.linespec.LineSpecification;
import org.de.htwg.klara.transformers.events.ClassVarChangedEvent;
import org.de.htwg.klara.transformers.events.IincInsnEvent;
import org.de.htwg.klara.transformers.events.LineEndEvent;
import org.de.htwg.klara.transformers.events.LineStartEvent;
import org.de.htwg.klara.transformers.events.PrintAddedEvent;
import org.de.htwg.klara.transformers.events.ScopeReachedEvent;
import org.de.htwg.klara.transformers.events.TransformationEvent;
import org.de.htwg.klara.transformers.events.TransformationEventListener;
import org.de.htwg.klara.transformers.events.VarInsnEvent;
import org.de.htwg.klara.transformers.variable.LocalVariable;
import org.de.htwg.klara.utils.TransformUtils;
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

/**
 * A special node of a ASM-Event-Chain, that will generate a self defined set of events for methods to a list of listeners.
 * This allows a set of manipulations at once as well as notifications to other listeners about the changes made.
 * Also it allows several modules to manipulate methods with minimal code duplication.
 * @author mrs
 */
public class Transformer extends ClassNode {
	private String className = "";
	private LineSpecification lineScope = new LineSpecification();
	private LineNumberNode currentLine = null;
	private MethodNode currentMethod = null;
	private final Map<Integer, LocalVariable> currentScope = new HashMap<>();
	private final List<LocalVariableNode> futureVariables = new LinkedList<>();
	private boolean inLineScope = false;
	
	private List<TransformationEventListener> listeners = new LinkedList<>();

	public Transformer(int api, ClassVisitor cv) {
		super(api);
		this.cv = cv;
	}
	
	/**
	 * Add a listener. Listener can only be added once.
	 * @param listener	The listener to add.
	 */
	public void addListener(TransformationEventListener listener) {
		removeListener(listener);
		listeners.add(listener);
	}
	
	/**
	 * Remove a listener.
	 * @param listener	The listener to remove
	 * @return	{@code true} if the action was successful, {@code false} otherwise
	 */
	public boolean removeListener(TransformationEventListener listener) {
		return listeners.remove(listener);
	}
	
	/**
	 * Set a line scope for which events will be generated. Any instructions in a line outside the scope will not generate events.
	 * Can be updated any time but will only be refreshed as soon as the next line is reached.
	 * @param scope	The line scope to use.
	 */
	public void setLineScope(LineSpecification scope) {
		this.lineScope = scope;
	}
	
	/**
	 * Send a event triggered by the current line to all {@link #listeners}.
	 * If it is not {@link #inLineScope} no event will be send.
	 * @param e	The event to send
	 */
	private void sendEvent(TransformationEvent e) {
		if (!inLineScope)
			return;
		for (TransformationEventListener tel : listeners) {
			tel.handle(e);
		}
	}
	
	/**
	 * Set a new line, will also update the variable {@link #inLineScope}
	 * @param line	The new line
	 */
	private void setLine(LineNumberNode line) {
		if (currentLine != null)
			sendEvent(new LineEndEvent(currentLine));
		currentLine = line;
		if (currentLine != null) {
			inLineScope = lineScope.contains(line.line);
			sendEvent(new LineStartEvent(currentLine));
		} else {
			inLineScope = false;
		}
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
	
	/**
	 * Performs the {@link #methodTransform(MethodNode) methodTransform} for all methods of the current class.
	 */
	@SuppressWarnings("unchecked")
	synchronized private void transform() {
		for (MethodNode mn : (List<MethodNode>) methods) {
			if (mn.instructions.size() > 0)
				methodTransform(mn);
		}
	}
	
	/**
	 * Scans through the instructions of the method, creating events for relevant structures.
	 * Listeners can then react to those events and modify the method.
	 * @param mn	The method to transform
	 */
	@SuppressWarnings("unchecked")
	synchronized private void methodTransform(MethodNode mn) {
		boolean waitingForSuperCall = "<init>".equals(mn.name);
		currentMethod = mn;
		currentScope.clear();
		futureVariables.clear();
		setLine(TransformUtils.guessMethodStart(mn));
		
		for (LocalVariableNode lvn : (List<LocalVariableNode>)mn.localVariables) {
			futureVariables.add(lvn);
		}
		
		if (mn.maxLocals > 0 && mn.localVariables.isEmpty()) {
			System.err.println("Warning: Method " + mn.name + " of class " + getClassName() + " has no debug information! Unable to track local variables.");
		}
		
		InsnList insns = mn.instructions;
		Iterator<AbstractInsnNode> j = insns.iterator();
		while (j.hasNext()) {
			AbstractInsnNode in = j.next();
			if (waitingForSuperCall) {
				if(in.getOpcode() == Opcodes.INVOKESPECIAL)
					waitingForSuperCall = false;
			} else if (in.getType() == AbstractInsnNode.VAR_INSN) {
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
					tmpNode = currentScope.get(scopeKeys[i]).getNode();
					if (label.equals(tmpNode.end)) {
						currentScope.remove(scopeKeys[i]);
					}
				}
				for (int i = 0; i < futureVariables.size(); ++i) {
					tmpNode = futureVariables.get(i);
					if (label.equals(tmpNode.start)) {
						currentScope.put(tmpNode.index, new LocalVariable(tmpNode));
						futureVariables.remove(tmpNode);
						--i;
						sendEvent(new ScopeReachedEvent(label, new LocalVariable(tmpNode)));
					}
				}
			} else if (in.getType() == AbstractInsnNode.LINE && currentLine.line != 0) {
				LineNumberNode lnn = (LineNumberNode)in;
				setLine(lnn);
			} else if (in.getType() == AbstractInsnNode.FIELD_INSN) {
				FieldInsnNode fin = (FieldInsnNode)in;
				if ((fin.getOpcode() == Opcodes.PUTSTATIC || fin.getOpcode() == Opcodes.PUTFIELD)
						&& fin.owner.equals(className)) {
					sendEvent(new ClassVarChangedEvent(fin));
				}
			}
		}
	}
	
	/**
	 * Inserts a print with the current location and any additional text in the {@code InsnList what} after the Node identified by {@code where}.
	 * @param where	The Node after which the print should be added
	 * @param what	A sequence of append calls to the StringBuilder on the stack. Can be an empty list.
	 */
	public void printLine(AbstractInsnNode where, InsnList what) {
		InsnList il = new InsnList();
		il.add(new FieldInsnNode(Opcodes.GETSTATIC, "org/de/htwg/klara/transformers/OutputStreamProvider", "stream", "Ljava/io/PrintStream;"));
		il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
		il.add(new InsnNode(Opcodes.DUP));
		il.add(new LdcInsnNode(TransformUtils.formatLocation(getClassName(), currentLine != null ? currentLine.line : 0)));
		il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
		il.add(what);
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
		il.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
		currentMethod.instructions.insert(where, il);
		sendEvent(new PrintAddedEvent(where));
	}
	
	/**
	 * Get the name of the class that is getting transformed, including all packages.
	 * Format is equal to the format in a import statement.
	 * @return	The full qualified class names of the current class, splitted using dots.
	 */
	public String getClassName() {
		return className.replace('/', '.');
	}
	
	/**
	 * Get a local Variable valid in the context of the current line by it's index.
	 * @param index	The index of the local variable
	 * @return	The local variable
	 */
	public LocalVariable getVar(int index) {
		return currentScope.get(index);
	}
}
