package org.example;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.*;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class ClassNullableChecker {
    private final HashMap<Integer, ArrayList<AbstractInsnNode>> instructinHashMap = new HashMap<>();
    public void checkAllNull() throws IOException, AnalyzerException {
        ClassReader reader = new ClassReader("org.example.TestCase");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        List<MethodNode> methods = classNode.methods;

        for (MethodNode methodNode : methods) {
            InsnList m_instructionList = methodNode.instructions;
            int previousLineNumber = 0;
            ArrayList<AbstractInsnNode> instructionsSet = new ArrayList<>();

            for (int count = 0; count < m_instructionList.size(); count++) {
                AbstractInsnNode instruction = m_instructionList.get(count);

                if(instruction.getClass() == LineNumberNode.class) {
                    if(previousLineNumber != 0) {
                        instructinHashMap.put(previousLineNumber, instructionsSet);
                        instructionsSet = new ArrayList<>();
                    }

                    previousLineNumber = ((LineNumberNode)instruction).line;
                } else {
                    instructionsSet.add(instruction);
                }
                excessiveNullCheck(classNode, methodNode, instruction);
            }

            //System.out.println(instructinHashMap);
        }
    }

    private void excessiveNullCheck(
            ClassNode classNode,
            MethodNode methodNode,
            AbstractInsnNode instruction
    ) throws AnalyzerException {
        AbstractInsnNode previousInstruction = instruction.getPrevious();
        AbstractInsnNode nextInstruction = instruction.getNext();

        //System.out.println("Method name: " + methodNode.name);
        //System.out.println("Method instruction: " + instruction.getOpcode());

        if (instruction.getOpcode() == IFNULL || instruction.getOpcode() == IFNONNULL) {
            String checkedValue = getFrameValue(classNode, methodNode, previousInstruction);
            System.out.println(checkedValue);
            if(checkedValue != null) {
                System.out.println("Excessive Null Check "+checkedValue);
            }
        }

//        if (instruction.getOpcode() == IFNULL || instruction.getOpcode() == IFNONNULL) {
//            if (nextInstruction.getOpcode() != RETURN) {
//                if (getFrameValue(classNode, methodNode, previousInstruction) == null) {
//                    System.out.println("contains excessive: " + instruction.getOpcode());
//                }
//            }
//        }

//        if (instruction.getOpcode() == IFNONNULL) {
//            if (nextInstruction.getOpcode() != RETURN) {
//                if (getFrameValue(classNode, methodNode, previousInstruction) == null) {
//                    System.out.println("contains excessive: " + instruction.getOpcode());
//                }
//            }
//        }
    }

    private String getFrameValue(
            ClassNode classNode,
            MethodNode methodNode,
            AbstractInsnNode instruction
    ) throws AnalyzerException {
        String value = null;
        Analyzer<ConstantTracker.ConstantValue> analyzer = new Analyzer<>(new ConstantTracker());
        analyzer.analyze(classNode.name, methodNode);

        for (Frame<ConstantTracker.ConstantValue> frame : analyzer.getFrames()) {
            if (frame == null) continue;

            if (instruction.getOpcode() != Opcodes.ALOAD) continue;
            VarInsnNode vn = (VarInsnNode) instruction;
            //System.out.println("accessing variable # " + vn.var);
            ConstantTracker.ConstantValue var = frame.getLocal(vn.var);

            //System.out.println("\tcontains in " + var.type);

            value = Objects.equals(var.type.toString(), "R") ? "realNull" : String.valueOf(var);
        }
        //System.out.println("\tcontains " + value);
        return value;
    }
}