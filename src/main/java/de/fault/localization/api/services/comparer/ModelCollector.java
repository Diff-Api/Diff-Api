package de.fault.localization.api.services.comparer;

import de.fault.localization.api.utilities.ComparerUtil;
import de.fault.localization.api.utilities.ReflectionUtil;
import lombok.Getter;
import lombok.val;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * this class holds both old and new models of the application
 */
@Getter
class ModelCollector {
	private final Set<Class<?>> oldEntities = new LinkedHashSet<>(), newEntities = new LinkedHashSet<>();

	/**
	 * adds all entries found from the other collector
	 *
	 * @param modelCollector - the collector to join
	 */
	void join(final ModelCollector modelCollector) {
		this.oldEntities.addAll(modelCollector.oldEntities);
		this.newEntities.addAll(modelCollector.newEntities);
	}

	/**
	 * @return deep copy of found entries
	 */
	Set<Class<?>> listEntities() {
		val res = new LinkedHashSet<>(this.oldEntities);
		res.addAll(this.newEntities);
		return res;
	}

	/**
	 * @param old - the old entity
	 */
	void addOldEntity(final Class<?> old) {
		if (!ComparerUtil.isEnumOrJava(old) || this.oldEntities.contains(old)) {
			return;
		}
		this.oldEntities.add(old);
		for (val field : old.getDeclaredFields()) {
			val genericType = ReflectionUtil.getGenericType(field);
			val type = field.getType();

			this.addOldEntity(genericType);
			this.addOldEntity(type);
		}

	}

	/**
	 * @param newOne - the new entity
	 */
	void addNewEntity(final Class<?> newOne) {
		if (!ComparerUtil.isEnumOrJava(newOne) || this.newEntities.contains(newOne)) {
			return;
		}
		this.newEntities.add(newOne);
		for (val field : newOne.getDeclaredFields()) {
			val genericType = ReflectionUtil.getGenericType(field);
			val type = field.getType();

			this.addNewEntity(genericType);
			this.addNewEntity(type);
		}
	}

	/**
	 * @return all entities that could have been changed
	 */
	private Map<Class<?>, Class<?>> getCompareSet() {
		final HashMap<Class<?>, Class<?>> map = new HashMap<>();
		for (val oldEntity : this.oldEntities) {
			if (!ComparerUtil.isEnumOrJava(oldEntity)) {
				continue;
			}
			val match = this.newEntities.stream().sequential().filter(s -> s.getName().equals(oldEntity.getName()))
					.collect(Collectors.toList());
			if (!match.isEmpty()) {
				map.put(oldEntity, match.get(0));
			}
		}
		for (val newEntity : this.newEntities) {
			if (!ComparerUtil.isEnumOrJava(newEntity)) {
				continue;
			}
			val match = this.oldEntities.stream().sequential()
					.filter(oldEntity -> oldEntity.getName().equals(newEntity.getName())).collect(Collectors.toList());
			if (!match.isEmpty()) {
				map.put(match.get(0), newEntity);
			}
		}

		return map;
	}

	/**
	 * @return all entities that have been changed
	 */
	Map<Class<?>, Class<?>> getChangedModels() {
		final Map<Class<?>, Class<?>> changedModels = new LinkedHashMap<>();
		for (val entry : this.getCompareSet().entrySet()) {
			val oldCls = entry.getKey();
			val newCls = entry.getValue();
			final String diff = ComparerUtil.compareClassMarkdown(oldCls, newCls);
			if (StringUtils.isEmpty(diff)) {
				continue;
			}
			changedModels.put(oldCls, newCls);
			changedModels.putAll(ReflectionUtil.listRecursiveChangedPublicFields(oldCls, newCls));
		}
		return changedModels;
	}
}
