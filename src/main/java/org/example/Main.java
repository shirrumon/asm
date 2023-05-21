package org.example;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class Main {
    public static void main(String[] args) throws IOException, AnalyzerException {
//        /*CLASS WRITER*/
//        ClassWriter cw = new ClassWriter(0);
//        cw.visit(
//                V1_5,
//                ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
//                "pkg/Comparable",
//                null,
//                "java/lang/Object",
//                null);
//
//        cw.visitField(
//                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
//                "LESS",
//                "J",
//                null,
//                -1L).visitEnd();
//
//        cw.visitField(
//                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
//                "EQUAL",
//                "I",
//                null,
//                0).visitEnd();
//
//        cw.visitField(
//                ACC_PUBLIC + ACC_FINAL + ACC_STATIC,
//                "GREATER",
//                "I",
//                null,
//                1).visitEnd();
//
//        cw.visitMethod(
//                ACC_PUBLIC + ACC_ABSTRACT,
//                "compareTo",
//                "(Ljava/lang/object;)I",
//                null,
//                null
//        ).visitEnd();
//
//        cw.visitEnd();
//        byte[] b = cw.toByteArray();

//        /*CLASS LOADER*/
//        MyClassLoader myClassLoader = new MyClassLoader();
//        Class c = myClassLoader.defineClass("pkg.Comparable", b);
//
//        /*CLASS READER*/
//        ClassPrinter cp = new ClassPrinter(ASM6);
//        ClassReader cr = new ClassReader(b);
//        cr.accept(cp, 0);

        /*CLASS NUllABLE CHECKER*/
//        ClassVisitor cv = new ClassVisitor(ASM6){};
//        ClassNullableChecker nc = new ClassNullableChecker(ASM6, cv);
//        ClassReader cnr = new ClassReader("org.example.TestCase");
//        cnr.accept(nc, 0);


        ClassReader reader = new ClassReader("org.example.TestCase");
        ClassNode classNode = new ClassNode();
        reader.accept(classNode,0);

        List<FieldNode> fields = classNode.fields;

//        for(FieldNode m: fields){
//            System.out.println("field name: " + m.name+"\n"+"field value: "+m.value);
//        }

        ClassNullableChecker cnc = new ClassNullableChecker();

        cnc.checkAllNull();
        //analyze();
    }

    private static void analyze() throws IOException, AnalyzerException {
        ClassReader cr = new ClassReader("org.example.TestCase");
        ClassNode cn = new ClassNode(ASM8);
        cr.accept(cn, 0);

        for(MethodNode mn: cn.methods) {
            Analyzer<ConstantTracker.ConstantValue> analyzer
                    = new Analyzer<>(new ConstantTracker());
            analyzer.analyze(cn.name, mn);
            int i = -1;
            for(Frame<ConstantTracker.ConstantValue> frame: analyzer.getFrames()) {
                i++;
                if(frame == null) continue;
                AbstractInsnNode n = mn.instructions.get(i);
                if(n.getOpcode() != Opcodes.ILOAD) continue;
                VarInsnNode vn = (VarInsnNode)n;
                System.out.println("accessing variable # "+vn.var);
                ConstantTracker.ConstantValue var = frame.getLocal(vn.var);
                System.out.println("\tcontains "+var);
            }
        }
    }
}