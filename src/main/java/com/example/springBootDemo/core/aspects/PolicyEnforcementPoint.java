package com.example.springBootDemo.core.aspects;

import com.example.springBootDemo.core.annotations.EntityClass;
import com.example.springBootDemo.core.annotations.SecureCaseAction;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@ConditionalOnProperty(name = "project.libraries.framework.security.enabled", havingValue = "true")
@AllArgsConstructor
public class PolicyEnforcementPoint {

    @Before("@annotation(secureCaseAction)")
    public void processBeforeCaseAction(JoinPoint point, SecureCaseAction secureCaseAction) {
        System.out.println("In PolicyEnforcementPoint Aspect ");
        final Class serviceClass = point.getTarget().getClass();
        EntityClass entityClassAnnotation = AnnotationUtils.findAnnotation(serviceClass, EntityClass.class);
        Object[] methodArgs = point.getArgs();
        if (methodArgs[0] instanceof String && "error".equalsIgnoreCase((String) methodArgs[0])) {
            // If we throw the exception here then the targeted method (method with SecureCaseAction Annotation)
            // won't be executed.
            throw new RuntimeException("case action \"" + methodArgs[0] + "\" doesn't have access. The annotation value is :"
                    + secureCaseAction.value());
        }

        // Class entityClass = Objects.requireNonNull(entityClassAnnotation).value();
    }




	/*@Around("@annotation(securePropertyRead)")
	public Object processAfterSecurePropertyRead(ProceedingJoinPoint point, SecurePropertyRead securePropertyRead) throws Throwable {
		Object retval = point.proceed();

		if (!(retval instanceof BaseEntityList) && !Collection.class.isAssignableFrom(retval.getClass()) && !AggregateInterface.class.isAssignableFrom(retval.getClass())) {
			BaseServiceInterface baseServiceInterface = entityResolverInterfaceImpl.getService(retval.getClass());
			Long entityId = getEntityId(point);
			if (!(SecurityAwareServiceInterface.class.isAssignableFrom(baseServiceInterface.getClass())))
				throw new InternalAssertionException("CANNOT USE SecurePropertyRead ANNOTATION ON CLASS THAT DOES NOT IMPLEMENT SecurityAwareServiceInterface");
			SecurityAwareServiceInterface securityAwareService = (SecurityAwareServiceInterface) baseServiceInterface;
			AclReferenceEntity aclReferenceEntity = AnnotationUtils.findAnnotation(retval.getClass(), AclReferenceEntity.class);
			if (aclReferenceEntity != null) {
				BaseEntityInterface referenceEntity = getAclReferenceEntity((BaseEntityInterface) retval, aclReferenceEntity.value());
				entityId = referenceEntity.getId();
				securityAwareService = (SecurityAwareServiceInterface) entityResolverInterfaceImpl.getService(referenceEntity.getClass());
			}
			HashSet<String> unreadablePropertiesRegexps = securityAwareService.getPropertiesRegexps(entityId, "UNREADABLE");
			ReflectionUtil.nullifyFieldsMatchingRegexps(retval, unreadablePropertiesRegexps);
		} else if (retval instanceof BaseEntityList) {
			Collection collectionOfEntities = ((BaseEntityList) retval).getResults();
			for (Object entity : collectionOfEntities) {
				BaseServiceInterface baseServiceInterface = entityResolverInterfaceImpl.getService(entity.getClass());
				if (!(SecurityAwareServiceInterface.class.isAssignableFrom(baseServiceInterface.getClass())))
					throw new InternalAssertionException("CANNOT USE SecurePropertyRead ANNOTATION ON CLASS THAT DOES NOT IMPLEMENT SecurityAwareServiceInterface");
				BaseEntityInterface baseEntity = (BaseEntityInterface) entity;
				final Long entityId = baseEntity.getId();
				SecurityAwareServiceInterface securityAwareService = (SecurityAwareServiceInterface) baseServiceInterface;
				HashSet<String> unreadablePropertiesRegexps = securityAwareService.getPropertiesRegexps(entityId, "UNREADABLE");
				ReflectionUtil.nullifyFieldsMatchingRegexps(baseEntity, unreadablePropertiesRegexps);
			}
		} else if (!(retval instanceof BaseEntityList) && Collection.class.isAssignableFrom(retval.getClass())) {
			Collection collectionOfEntities = (Collection) retval;
			for (Object entity : collectionOfEntities) {
				this.handleEntity(entity);
			}
		} else if (retval instanceof AggregateInterface) {
			String aggregateRootFieldName = ReflectionUtil.getSoleFieldNameWithAnnotation(retval.getClass(), AggregateRoot.class);
			BaseEntityInterface aggregateRootObject = (BaseEntityInterface) ReflectionUtil.getFieldValue(retval, aggregateRootFieldName);
			if (Objects.nonNull(aggregateRootObject)) {
				this.handleEntity(aggregateRootObject);
				this.handleAggregates(retval, aggregateRootObject);
			}
		}
		return retval;
	}

	//also check if service implements SecurityAwareServiceInterface
	@Before("@annotation(securePropertyMerge)")
	public void processBeforeSecurePropertyMerge(JoinPoint point, SecurePropertyMerge securePropertyMerge) throws Throwable {
		Long entityId = getEntityId(point);
		final Class serviceClass = point.getTarget().getClass();
		SecurityAwareServiceInterface securityAwareService = (SecurityAwareServiceInterface) ApplicationContextHolder.getApplicationContext().getBean(serviceClass);
		EntityClass entityClassAnnotation = AnnotationUtils.findAnnotation(serviceClass, EntityClass.class);
		Class entityClass = Objects.requireNonNull(entityClassAnnotation).value();
		AclReferenceEntity aclReferenceEntity = AnnotationUtils.findAnnotation(entityClass, AclReferenceEntity.class);
		if (aclReferenceEntity != null) {
			BaseEntityInterface entity = ((BaseServiceInterface) point.getTarget()).getEntityById(entityId);
			BaseEntityInterface referenceEntity = getAclReferenceEntity(entity, aclReferenceEntity.value());
			if (Objects.nonNull(referenceEntity)) {
				entityId = referenceEntity.getId();
				securityAwareService = (SecurityAwareServiceInterface) entityResolverInterfaceImpl.getService(referenceEntity.getClass());
			}
		}
		HashSet<String> unupdateablePropertiesRegexps = securityAwareService.getPropertiesRegexps(entityId, "UNUPDATEABLE");
		if (point.getArgs().length > 2 && point.getArgs()[2] instanceof ArrayList) {
			ArrayList<Object> oldChangedAttrs = (ArrayList<Object>) point.getArgs()[2];

			oldChangedAttrs.stream().forEach(field -> unupdateablePropertiesRegexps.forEach(unupdateablePropertiesRegexp -> {
				Pattern pattern = Pattern.compile(unupdateablePropertiesRegexp);
				Matcher matcher = pattern.matcher((String) field);
				if (matcher.find()) {

					throw new UnauthorizedException();
				}
			}));
		}
	}


	@Before("@annotation(secureLinkedEntityCaseAction)")
	public void processBeforeSecureLinkedEntityCaseAction(JoinPoint point, SecureLinkedEntityCaseAction secureLinkedEntityCaseAction) {
		final Set<EntityLink> entityLinks = getLinkedEntities(point, secureLinkedEntityCaseAction.links());
		this.checkForSecureLinkedEntityCaseAction(entityLinks, secureLinkedEntityCaseAction.caseAction());
	}

	public void checkForSecureLinkedEntityCaseAction(Set<EntityLink> entityLinks, String operation) {
		entityLinks.forEach(entityLink -> {
			Class linkedEntityClass = entityLink.getEClass();
			Long linkedEntityId = entityLink.getId();
			ServiceClass serviceClass = AnnotationUtils.findAnnotation(linkedEntityClass, ServiceClass.class);
			Class linkedEntityServiceClass = serviceClass.value();
			SecurityAwareServiceInterface linkedSecurityAwareService = (SecurityAwareServiceInterface) ApplicationContextHolder.getApplicationContext().getBean(linkedEntityServiceClass);
			Set<CaseAction> caseActions = linkedSecurityAwareService.getCaseActions(linkedEntityId);
			Optional<CaseAction> caseActionAvailable = caseActions.stream().filter(caseAction -> caseAction.getCaseAction().equals(operation)).findFirst();
			if (!caseActionAvailable.isPresent() || !caseActionAvailable.get().getIsAllowed())
				throw new UnauthorizedException();
		});
	}

	@Before("@annotation(secureCaseAction)")
	public void processBeforeCaseAction(JoinPoint point, SecureCaseAction secureCaseAction) {
		final Class serviceClass = point.getTarget().getClass();
		EntityClass entityClassAnnotation = AnnotationUtils.findAnnotation(serviceClass, EntityClass.class);
		Class entityClass = Objects.requireNonNull(entityClassAnnotation).value();
		AclReferenceEntity aclReferenceEntity = AnnotationUtils.findAnnotation(entityClass, AclReferenceEntity.class);
		Long entityId = getEntityId(point);
		SecurityAwareServiceInterface securityAwareService = (SecurityAwareServiceInterface) ApplicationContextHolder.getApplicationContext().getBean(serviceClass);
		if (aclReferenceEntity != null) {
			BaseEntityInterface entity = ((BaseServiceInterface) point.getTarget()).getEntityById(entityId);
			BaseEntityInterface referenceEntity = getAclReferenceEntity(entity, aclReferenceEntity.value());
			entityId = referenceEntity.getId();
			securityAwareService = (SecurityAwareServiceInterface) entityResolverInterfaceImpl.getService(referenceEntity.getClass());
		}
		String operation = secureCaseAction.value();
		if (Objects.nonNull(entityId)) {
			Set<CaseAction> caseActions = securityAwareService.getCaseActions(entityId);
			Optional<CaseAction> caseActionAvailable = caseActions.stream().filter(caseAction -> caseAction.getCaseAction().equals(operation)).findFirst();
			if (!caseActionAvailable.isPresent() || !caseActionAvailable.get().getIsAllowed())
				throw new UnauthorizedException();
		} else {
			Set<CaseAction> authorizedCaseActions = securityAwareService.getCaseActions(entityClass);
			Optional<CaseAction> caseActionAvailable = authorizedCaseActions.stream().filter(caseAction -> caseAction.getCaseAction().equals(operation)).findFirst();
			if (!caseActionAvailable.isPresent() || !caseActionAvailable.get().getIsAllowed())
				throw new UnauthorizedException();

		}
	}

	@Around("@annotation(secureFetchAction)")
	public Object processBeforeSecureFetch(ProceedingJoinPoint point, SecureFetchAction secureFetchAction) throws Throwable {
		final Class serviceClass = point.getTarget().getClass();
		BaseServiceInterface baseServiceInterface = (BaseServiceInterface) ApplicationContextHolder.getApplicationContext().getBean(serviceClass);
		Class entityClass = entityResolverInterfaceImpl.getEntityClass(serviceClass);

		if (!(SecurityAwareServiceInterface.class.isAssignableFrom(baseServiceInterface.getClass())))
			throw new InternalAssertionException("CANNOT USE SecurePropertyRead ANNOTATION ON CLASS THAT DOES NOT IMPLEMENT SecurityAwareServiceInterface");
		SecurityAwareServiceInterface securityAwareService = (SecurityAwareServiceInterface) baseServiceInterface;
		Set<String> fetchRules = securityAwareService.getFetchRulesByAuthorizedCaseAction(entityClass, "FETCH");
		String generatedCriteria = fetchRules.stream().map(fetchRule -> "( " + fetchRule.replace("${loggedInUser}", securityAwareService.getPrincipal()) + " )")
				.collect(Collectors.joining(" OR "));


		MethodSignature signature = (MethodSignature) point.getSignature();
		String methodName = signature.getMethod().getName();
		Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
		Annotation[][] annotations = point.getTarget().getClass().getMethod(methodName, parameterTypes).getParameterAnnotations();
		*//*Set<Class<?>> parameterTypesWithSecureFetchCriteria = IntStream.range(0, annotations.length)
				.filter(i -> {
					Optional<Annotation> foundSecureFetchCriteria = Arrays.stream(annotations[i]).filter(annotation -> annotation.annotationType().equals(SecureFetchCriteria.class)).findFirst();
					return foundSecureFetchCriteria.isPresent();
				})
				.mapToObj(i -> parameterTypes[i])
				.collect(Collectors.toSet());*//*
		Set<Integer> indices = IntStream.range(0, annotations.length)
				.filter(i -> {
					Optional<Annotation> foundSecureFetchCriteria = Arrays.stream(annotations[i]).filter(annotation -> annotation.annotationType().equals(SecureFetchCriteria.class)).findFirst();
					return foundSecureFetchCriteria.isPresent();
				})
				.mapToObj(i -> i)
				.collect(Collectors.toSet());
		if (indices.size() != 1) {
			throw new InternalAssertionException("METHOD ANNOTATED WITH SecureFetchAction MUST HAVE EXACTLY 1 PARAMETER ANNOTATED WITH SecureFetchCriteria");
		}
		Optional<Integer> foundIndex = indices.stream().findFirst();
		Object[] args = point.getArgs();
		Object criteria = args[foundIndex.get().intValue()];
		if (!(criteria instanceof String)) {
			throw new InternalAssertionException("SecureFetchCriteria CAN ONLY BE USED ON STRING TYPE PARAMETER");
		}

		if ((Objects.isNull(criteria) || ((String) criteria).length() == 0) &&
				(Objects.nonNull(generatedCriteria) && generatedCriteria.length() > 0)) {
			args[foundIndex.get().intValue()] = generatedCriteria;
			return point.proceed(args);
		} else if ((Objects.nonNull(criteria) && ((String) criteria).length() > 0) &&
				(Objects.nonNull(generatedCriteria) && generatedCriteria.length() > 0)) {
			args[foundIndex.get().intValue()] = "( " + criteria + " )" + " AND " + "( " + generatedCriteria + " )";
			return point.proceed(args);
		} else {
			return point.proceed();
		}
	}

	private Long getEntityType(JoinPoint point) {
		return null;
	}

	//TODO this method assumes that the first argument will be either id or entity, what if second one is id or entity
	private Long getEntityId(JoinPoint point) {
		Long entityId;
		if (point.getArgs()[0] instanceof Long)
			entityId = (Long) point.getArgs()[0];
		else if (point.getArgs()[0] instanceof BaseEntityInterface) {
			entityId = ((BaseEntityInterface) point.getArgs()[0]).getId();
		} else if (point.getArgs()[0] instanceof AggregateInterface) {
			entityId = getEntityIdForAggregateInterface((AggregateInterface) point.getArgs()[0]);
		} else {
			throw new EntityIdNotFoundException();
		}
		return entityId;
	}

	private Long getEntityIdForAggregateInterface(AggregateInterface aggregate) {
		String fieldWithAggregateRoot = ReflectionUtil.getSoleFieldNameWithAnnotation(aggregate.getClass(), AggregateRoot.class);
		if (fieldWithAggregateRoot != null) {
			return ((BaseEntityInterface) ReflectionUtil.getFieldValue(aggregate, fieldWithAggregateRoot)).getId();
		}
		String fieldWithLinkTo = ReflectionUtil.getSoleFieldNameWithAnnotation(aggregate.getClass(), LinkTo.class);
		if (fieldWithLinkTo != null) {
			return ((BaseEntityInterface) ReflectionUtil.getFieldValue(aggregate, fieldWithLinkTo)).getId();
		}
		return null;
	}

	//TODO this method assumes that the second argument will be Set<EntityLink<BaseEntityInterface>>, what if second one is id or entity
	private Set<EntityLink> getLinkedEntities(JoinPoint point, Class[] links) {
		Set<EntityLink> entityLinks;
		if( point.getArgs().length >= 2 && point.getArgs()[1] instanceof Set) {
			Set<Object> tempEntityLinks = (Set<Object>)point.getArgs()[1];
			tempEntityLinks.forEach(entityLink -> {
				if ( !(entityLink instanceof EntityLink)) {
					throw new EntityIdNotFoundException();
				}
			});
			entityLinks = (Set<EntityLink>)point.getArgs()[1];
			if (links.length > 0) {
				Set<EntityLink> definedEntityLinks = new HashSet<>();
				tempEntityLinks.stream().forEach(entity -> {
					if (Arrays.asList(links).contains(((EntityLink) entity).getEClass())) {
						definedEntityLinks.add((EntityLink) entity);
					}
				});
				entityLinks = definedEntityLinks;
			}
		} else {
			throw new InternalAssertionException("method arguments do not match the required condition");
		}
		return entityLinks;
	}

	private void handleAggregates(Object retval, Object aggregateRootObject) {
		List<String> aggregateFieldNames = ReflectionUtil.getFieldNamesWithAnnotation(retval.getClass(), Aggregate.class);
		for (String aggregateFieldName : aggregateFieldNames) {
			Object aggregateObject = ReflectionUtil.getFieldValue(retval, aggregateFieldName);
			if (Objects.nonNull(aggregateObject)) {
				if (Collection.class.isAssignableFrom(aggregateObject.getClass())) {
					for (Object aggregateItem : (Collection) aggregateObject) {
						this.handleAggregates(aggregateItem, aggregateRootObject);
					}
				} else {
					this.handleAggregates(aggregateObject, aggregateRootObject);
				}
			}
		}
		List<String> linkToFieldNames = ReflectionUtil.getFieldNamesWithAnnotation(retval.getClass(), LinkTo.class);
		for (String linkToFieldName : linkToFieldNames) {
			BaseEntityInterface linkToObject = (BaseEntityInterface) ReflectionUtil.getFieldValue(retval, linkToFieldName);
			this.handleEntity(linkToObject);
		}
	}

	private void handleEntity(Object entity) {
		BaseServiceInterface baseServiceInterface = null;
		AclReferenceEntity aclReferenceEntity = AnnotationUtils.getAnnotation(entity.getClass(), AclReferenceEntity.class);
		BaseEntityInterface referenceEntity = null;
		if (aclReferenceEntity != null) {
			Class referenceEntityClass = aclReferenceEntity.value();
			referenceEntity = getAclReferenceEntity((BaseEntityInterface) entity, referenceEntityClass);
			baseServiceInterface = entityResolverInterfaceImpl.getService(referenceEntityClass);
		} else {
			baseServiceInterface = entityResolverInterfaceImpl.getService(entity.getClass());
		}
		if (!(SecurityAwareServiceInterface.class.isAssignableFrom(baseServiceInterface.getClass())))
			throw new InternalAssertionException("CANNOT USE SecurePropertyRead ANNOTATION ON CLASS THAT DOES NOT IMPLEMENT SecurityAwareServiceInterface");
		BaseEntityInterface baseEntity = referenceEntity != null ? referenceEntity : (BaseEntityInterface) entity;
		// final Long entityId = baseEntity.getId();
		SecurityAwareServiceInterface securityAwareService = (SecurityAwareServiceInterface) baseServiceInterface;
		HashSet<String> unreadablePropertiesRegexps = securityAwareService.getPropertiesRegexps(baseEntity, "UNREADABLE");
		ReflectionUtil.nullifyFieldsMatchingRegexps(entity, unreadablePropertiesRegexps);
	}

	private BaseEntityInterface getAclReferenceEntity(BaseEntityInterface entity, Class referenceEntityClass) {
		String referenceFieldName = ReflectionUtil.getFieldNameWithType(entity.getClass(), referenceEntityClass);
		if (Objects.nonNull(referenceFieldName)) {
			return (BaseEntityInterface) ReflectionUtil.getFieldValue(entity, referenceFieldName);
		}
		return getAclReferenceEntityRecursively(entity, referenceEntityClass);
	}

	private BaseEntityInterface getAclReferenceEntityRecursively(BaseEntityInterface entity, Class referenceEntityClass) {
		List<Class> relatedEntityFieldTypes = new ArrayList<>();
		relatedEntityFieldTypes.addAll(ReflectionUtil.getFieldTypesWithAnnotation(entity.getClass(), OneToMany.class));
		relatedEntityFieldTypes.addAll(ReflectionUtil.getFieldTypesWithAnnotation(entity.getClass(), OneToOne.class));
		relatedEntityFieldTypes.addAll(ReflectionUtil.getFieldTypesWithAnnotation(entity.getClass(), ManyToOne.class));
		for (Class fieldType: relatedEntityFieldTypes) {
			String referenceFieldName = ReflectionUtil.getFieldNameWithType(fieldType, referenceEntityClass);
			if (Objects.nonNull(referenceFieldName)) {
				BaseEntityInterface linkedEntity = (BaseEntityInterface) ReflectionUtil.getFieldValueByType(entity, fieldType);
				Object referenceEntity = ReflectionUtil.getFieldValue(linkedEntity, referenceFieldName);
				if (Objects.nonNull(referenceEntity)) {
					return (BaseEntityInterface) referenceEntity;
				}
			}
		}
		for (Class fieldType: relatedEntityFieldTypes) {
			BaseEntityInterface baseEntityInterface = (BaseEntityInterface) ReflectionUtil.getFieldValueByType(entity, fieldType);
			return getAclReferenceEntityRecursively(baseEntityInterface, referenceEntityClass);
		}
		return null;
	}*/

}
