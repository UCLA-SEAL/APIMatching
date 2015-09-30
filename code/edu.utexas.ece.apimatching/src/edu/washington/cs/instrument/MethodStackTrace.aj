package edu.washington.cs.instrument;

import java.io.PrintStream;

import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import edu.washington.cs.profile.MethodStackTraceFile;
import edu.washington.cs.rules.JavaMethod;

public aspect MethodStackTrace issingleton(){

	public static boolean instrumentationOn ;

	static {
		instrumentationOn= InstrumentationConfiguration.METHOD_STACKTRACE_ON;
		System.err.print("METHOD STACK TRACE ON/OFF: "+instrumentationOn);
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
				MethodStackTraceFile.println(jm, new Throwable());
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
			if (jm!=null && UnmatchedMethod.contains(jm)){
				MethodStackTraceFile.println(jm, new Throwable());
			}
		}
	}

}
