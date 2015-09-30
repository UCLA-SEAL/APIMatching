package edu.washington.cs.instrument;

import java.io.PrintStream;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import edu.washington.cs.profile.MethodTraceFile;
import edu.washington.cs.rules.JavaMethod;

public aspect MethodTraceForCount issingleton(){

	public static boolean instrumentationOn ;
	static {
		instrumentationOn= InstrumentationConfiguration.METHOD_COUNT_ON;
		System.err.print("METHODTRACE ON/OFF: "+instrumentationOn);
	}
	private PrintStream print_stream = null;
	private pointcut all_methods(): execution (* *(..))&& !within (edu.washington..*);

	before() : all_methods() {
		if (instrumentationOn) {
			Signature sig = thisJoinPoint.getSignature();
			JavaMethod jm = null;
			if (sig instanceof MethodSignature) {
				MethodSignature ms = (MethodSignature) sig;
				jm = new JavaMethod(ms);
			}
			if (jm!=null && UnmatchedMethod.contains(jm)){
				MethodTraceFile.println(jm.toString());
			}
		}
	}
	private pointcut all_constructors(): execution(*.new(..)) &&!within(edu.washington..*);
	before() : all_constructors() { 
		if (instrumentationOn) {
			Signature sig = thisJoinPoint.getSignature();
			JavaMethod jm = null;
			
			if (sig instanceof ConstructorSignature) {
				ConstructorSignature cs = (ConstructorSignature) sig;
				jm = new JavaMethod(cs);
			}
			if (jm!= null && UnmatchedMethod.contains(jm)){
				MethodTraceFile.println(jm.toString());
			}
		}
	}
}
