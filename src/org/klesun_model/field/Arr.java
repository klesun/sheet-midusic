package org.klesun_model.field;

import org.sheet_midusic.staff.MidianaComponent;
import org.klesun_model.AbstractModel;
import org.klesun_model.IModel;
import org.sheet_midusic.stuff.tools.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// this class represents our model field that stores collections of AbstractModel-z
// TODO: it's not array anymore, it's collection - rename it!
public class Arr<ELEM_CLASS extends AbstractModel> extends Field<Collection<ELEM_CLASS>>
{
	Class<ELEM_CLASS> elemClass;
	Class<? extends Collection<ELEM_CLASS>> collectionClass;

	public Arr(String name, Collection<ELEM_CLASS> value, IModel owner, Class<ELEM_CLASS> elemClass) {
		super(name, value, owner);
		this.elemClass = elemClass;
		this.collectionClass = (Class<Collection<ELEM_CLASS>>)value.getClass();
	}

	@Override
	public JSONArray getJsonValue() {
		JSONArray arr = new JSONArray("[]");
		for (AbstractModel el: get()) {
			if (el.getJsonRepresentation().keySet().size() != 0 || !omitDefaultFromJson()) {
				arr.put(el.getJsonRepresentation());
			}
		}
		return arr;
	}

	@Override
	public void setValueFromJsObject(JSONObject jsObject) {
		get().clear();
		JSONArray arr = jsObject.getJSONArray(getName());
		for (int i = 0; i < arr.length(); ++i) {
			ELEM_CLASS el = null;
			if (MidianaComponent.class.isAssignableFrom(elemClass)) {
				try {
					el = elemClass.getDeclaredConstructor(owner.getClass()).newInstance(owner);
				} catch (Exception e) {
					Logger.fatal(e, "Failed to make instance of {" + elemClass.getSimpleName() + "}");
				}
			} else if (AbstractModel.class.isAssignableFrom(elemClass)) {
				try {
					el = elemClass.newInstance();
				} catch (Exception e) {
					Logger.fatal(e, "Common, every class has an empty constructor in java! {" + elemClass.getSimpleName() + "}");
				}
			} else {
				Logger.fatal("Should never happen, right?");
			}

			el.reconstructFromJson(arr.getJSONObject(i)); // it's important to do reconstructFromJson before add, cuz the Collection may be a set
			get().add(el);
		}
	}

	public ELEM_CLASS get(int index) {
		return elemClass.cast(get().toArray()[index]);
	}

	public ELEM_CLASS add(ELEM_CLASS elem) {
		get().add(elem);
		set(get()); // problem? ... it's for onChange() lambda to be called
		return elem;
	}
	public ELEM_CLASS add(ELEM_CLASS elem, int index) {
		List<ELEM_CLASS> newList = new ArrayList<>(get());
		newList.add(index, elem);
		setFromList(newList);
		return elem;
	}

	public void remove(ELEM_CLASS elem) {
		get().remove(elem);
		set(get()); // problem? ... it's for onChange() lambda to be called
	}

	public int size() {
		return get().size();
	}

	public int indexOf(ELEM_CLASS elem) {
		return new ArrayList<>(get()).indexOf(elem);
	}

	private void setFromList(List<ELEM_CLASS> list)
	{
		try { set(collectionClass.getDeclaredConstructor(Collection.class).newInstance(list)); }
		catch (NoSuchMethodException exc) { Logger.fatal(exc, "I dont believe you! What is it a Collection, that cannot be created from another Collection?"); }
		catch (InstantiationException exc) { Logger.fatal(exc, "Abstract class? NO WAI!"); }
		catch (InvocationTargetException exc) { Logger.fatal(exc, "Bet it wont occur evar? I dont even know what this exception means"); }
		catch (IllegalAccessException exc) { Logger.fatal(exc, "Shouldn't private methods be private only on compilation time, like generics?"); } // heretic
	}

	@Override
	public Arr<ELEM_CLASS> setOmitDefaultFromJson(Boolean value) {
		return (Arr<ELEM_CLASS>)super.setOmitDefaultFromJson(value);
	}

	@Override
	protected void checkValueClass(Class cls) {
		// nothing can go wrong i suppose =P
	}

	@Override
	public Field setValueFromString(String str) {
		Logger.fatal("Not supported " + str);
		return this;
	}
}