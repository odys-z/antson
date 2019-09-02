package io.odysz.antson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import io.odysz.anson.Anson;

/**
 * @author javaBeCool @ https://stackoverflow.com/questions/1868333/how-can-i-determine-the-type-of-a-generic-field-in-java
 *
 */
public class ReflectionUtils {
	public static Class<?> determineType(Field field, Object object) {
	    Class<?> type = object.getClass();
	    return (Class<?>) getType(type, field).type;
	}

	protected static class TypeInfo {
	    Type type;
	    Type name;

	    public TypeInfo(Type type, Type name) {
	        this.type = type;
	        this.name = name;
	    }

	}

	private static TypeInfo getType(Class<?> clazz, Field field) {
	    TypeInfo type = new TypeInfo(null, null);
	    if (field.getGenericType() instanceof TypeVariable<?>) {
	        TypeVariable<?> genericTyp = (TypeVariable<?>) field.getGenericType();
	        Class<?> superClazz = clazz.getSuperclass();

	        if (clazz.getGenericSuperclass() instanceof ParameterizedType) {
	            ParameterizedType paramType = (ParameterizedType) clazz.getGenericSuperclass();
	            TypeVariable<?>[] superTypeParameters = superClazz.getTypeParameters();
	            if (!Object.class.equals(paramType)) {
	                if (field.getDeclaringClass().equals(superClazz)) {
	                    // this is the root class an starting point for this search
	                    type.name = genericTyp;
	                    type.type = null;
	                } else {
	                    type = getType(superClazz, field);
	                }
	            }
	            if (type.type == null || type.type instanceof TypeVariable<?>) {
	                // lookup if type is not found or type needs a lookup in current concrete class
	                for (int j = 0; j < superClazz.getTypeParameters().length; ++j) {
	                    TypeVariable<?> superTypeParam = superTypeParameters[j];
	                    if (type.name.equals(superTypeParam)) {
	                        type.type = paramType.getActualTypeArguments()[j];
	                        Type[] typeParameters = clazz.getTypeParameters();
	                        if (typeParameters.length > 0) {
	                            for (Type typeParam : typeParameters) {
	                                TypeVariable<?> objectOfComparison = superTypeParam;
	                                if(type.type instanceof TypeVariable<?>) {
	                                    objectOfComparison = (TypeVariable<?>)type.type;
	                                }
	                                if (objectOfComparison.getName().equals(((TypeVariable<?>) typeParam).getName())) {
	                                    type.name = typeParam;
	                                    break;
	                                }
	                            }
	                        }
	                        break;
	                    }
	                }
	            }
	        }
	    } else {
	    	if (field.getGenericType() instanceof ParameterizedType) {
	    		type.type = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0].getClass();

	    		// type.type = ((ParameterizedType)field.getGenericType()).getRawType();
	    		type.type = findActualType(clazz, type.name);
	    		if (type.type == null)
	    			type.type = ((ParameterizedType)field.getGenericType()).getRawType();
	    	}
	    	else 
	    		type.type = field.getGenericType();
	    }

	    return type;
	}

	private static Field getField(Object obj, String fieldName) {
		return getField (obj.getClass(), fieldName);
	}

	private static Field getField(Class<?> cls, String fieldName) {
		try {
			Field f = cls.getDeclaredField(fieldName);
			return f;
		} catch (NoSuchFieldException | SecurityException e) {
//			e.printStackTrace();
			Class<?> suprcls = cls.getSuperclass();
			if (suprcls != null && suprcls != Object.class)
				return getField(suprcls, fieldName);
		}
		return null;
	}

	private static Type findActualType(Class<?> cls, Type name) {
		// ParameterizedType paramizedCls = null;
		TypeVariable<?>[] typeParas = cls.getTypeParameters();
		// TypeVariable<?>[] paras = cls.getTypeParameters();
		if (typeParas == null || typeParas.length == 0)
			return null;

		for (int j = 0; j < typeParas.length; ++j) {
			TypeVariable<?> typeParam = typeParas[j];
			if (name.equals(typeParam)) {
//				return cls.getActualTypeArguments()[j];
				return null;
			}
			
		}
		return null;
	}

	class Ason4<T extends Anson> {
		ArrayList<T> strlst;
	}

//	@Test
//	public void testAson4 () {
//		Ason4<AnsT1> ason4 = new Ason4<AnsT1>();
//	    Field field = ReflectionUtils.getField(ason4, "strlst");
//	    Class<?> clazz = ReflectionUtils.determineType(field, ason4);
////	    Assert.assertEquals(clazz, Ason1.class);
//	}

	class GenericSuperClass<E, T, A> {
	    T t;
	    E e;
	    A a;
	    BigDecimal b;
	}
	
	class GenericDefinition extends GenericSuperClass<Integer, Integer, Integer> { }

	@Test
	public void testSimpleInheritanceTypeDetermination() {
	    GenericDefinition gd = new GenericDefinition();
	    Field field = ReflectionUtils.getField(gd, "t");
	    Class<?> clazz = ReflectionUtils.determineType(field, gd);
	    assertEquals(clazz, Integer.class);
	    field = ReflectionUtils.getField(gd, "b");
	    clazz = ReflectionUtils.determineType(field, gd);
	    assertEquals(clazz, BigDecimal.class);
	}

	class MiddleClass<A, E> extends GenericSuperClass<E, Integer, A> { }

	// T = Integer, E = String, A = Double
	class SimpleTopClass extends MiddleClass<Double, String> { }

	@Test
	public void testSimple2StageInheritanceTypeDetermination() {
	    SimpleTopClass stc = new SimpleTopClass();
	    Field field = ReflectionUtils.getField(stc, "t");
	    Class<?> clazz = ReflectionUtils.determineType(field, stc);
	    assertEquals(clazz, Integer.class);
	    field = ReflectionUtils.getField(stc, "e");
	    clazz = ReflectionUtils.determineType(field, stc);
	    assertEquals(clazz, String.class);
	    field = ReflectionUtils.getField(stc, "a");
	    clazz = ReflectionUtils.determineType(field, stc);
	    assertEquals(clazz, Double.class);
	}

	class TopMiddleClass<A> extends MiddleClass<A, Double> { }

	// T = Integer, E = Double, A = Float
	class ComplexTopClass extends TopMiddleClass<Float> {}

	@Test
	public void testComplexInheritanceTypDetermination() {
	    ComplexTopClass ctc = new ComplexTopClass();
	    Field field = ReflectionUtils.getField(ctc, "t");
	    Class<?> clazz = ReflectionUtils.determineType(field, ctc);
	    assertEquals(clazz, Integer.class);
	    field = ReflectionUtils.getField(ctc, "e");
	    clazz = ReflectionUtils.determineType(field, ctc);
	    assertEquals(clazz, Double.class);
	    field = ReflectionUtils.getField(ctc, "a");
	    clazz = ReflectionUtils.determineType(field, ctc);
	    assertEquals(clazz, Float.class);
	}

	class ConfusingClass<A, E> extends MiddleClass<E, A> {}
	// T = Integer, E = Double, A = Float ; this class should map between a and e
	class TopConfusingClass extends ConfusingClass<Double, Float> {}

	@Test
	public void testConfusingNamingConvetionWithInheritance() {
	    TopConfusingClass tcc = new TopConfusingClass();
	    Field field = ReflectionUtils.getField(tcc, "t");
	    Class<?> clazz = ReflectionUtils.determineType(field, tcc);
	    assertEquals(clazz, Integer.class);
	    field = ReflectionUtils.getField(tcc, "e");
	    clazz = ReflectionUtils.determineType(field, tcc);
	    assertEquals(clazz, Double.class);
	    field = ReflectionUtils.getField(tcc, "a");
	    clazz = ReflectionUtils.determineType(field, tcc);
	    assertEquals(clazz, Float.class);
	    field = ReflectionUtils.getField(tcc, "b");
	    clazz = ReflectionUtils.determineType(field, tcc);
	    assertEquals(clazz, BigDecimal.class);
	}

	class Pojo {
	    Byte z;
	}

	@Test
	public void testPojoDetermineType() {
	    Pojo pojo = new Pojo();
	    Field field = ReflectionUtils.getField(pojo, "z");
	    Class<?> clazz = ReflectionUtils.determineType(field, pojo);
	    assertEquals(clazz, Byte.class);
	}
}
