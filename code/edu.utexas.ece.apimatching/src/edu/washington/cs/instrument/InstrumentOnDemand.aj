package edu.washington.cs.instrument;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;

import edu.washington.cs.rules.JavaMethod;

public abstract aspect InstrumentOnDemand {

	
	protected static java.util.HashSet unmatchedMethod;  
	abstract void instrumentMethod (JoinPoint jp);
	abstract void instrumentConstructor (JoinPoint jp);
	private pointcut all_methods(): execution (* *(..))&& !within (edu.washington..*);

	before() : all_methods() {
		Signature sig = thisJoinPoint.getSignature();
		JavaMethod jm = null;
		
		if (sig instanceof ConstructorSignature) {
			ConstructorSignature cs = (ConstructorSignature) sig;
			jm = new JavaMethod(cs);
		}
		if (UnmatchedMethod.contains(jm)) {
			instrumentMethod(thisJoinPoint);	
		}
	}
	private pointcut all_constructors(): execution(*.new(..)) &&!within(edu.washington..*);
	before() : all_constructors() {
		Signature sig = thisJoinPoint.getSignature();
		JavaMethod jm = null;
		
		if (sig instanceof ConstructorSignature) {
			ConstructorSignature cs = (ConstructorSignature) sig;
			jm = new JavaMethod(cs);
		}
		if (UnmatchedMethod.contains(jm)) {
			instrumentConstructor(thisJoinPoint);
		}
	}
}
