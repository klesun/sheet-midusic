package org.shmidusic.sheet_music.staff.chord.note;


import org.klesun_model.AbstractModel;
import org.shmidusic.sheet_music.staff.staff_config.KeySignature;
import org.shmidusic.stuff.graphics.ImageStorage;
import org.klesun_model.field.Field;

import org.shmidusic.stuff.tools.INote;
import org.apache.commons.math3.fraction.Fraction;

import org.json.JSONObject;

// TODO: rename to Note
public class Note extends AbstractModel implements INote
{
	// <editor-fold desc="model field declaration">

	// TODO: normalization rules maybe ???
	final public Field<Integer> tune = new Field<>("tune", Integer.class, true, this, n -> limit(n, 0, 127));
	final protected Field<Integer> channel = new Field<>("channel", Integer.class, true, this, n -> limit(n, 0, 15));
	final public Field<Fraction> length = new Field<>("length", new Fraction(1, 4), this, INote::legnthNorm);

    /** @TODO: forbid more than two dots and move tripletness into length */
    final public Field<Boolean> isTriplet = new Field<>("isTriplet", false, this);
	/** @unused */
    final public Field<Boolean> isSharp = new Field<>("isSharp", false, this);
    /** @unused */
	final private Field<Boolean> isMuted = new Field<>("isMuted", false, this);
    /** @broken */
	final private Field<Boolean> isLinkedToNext = new Field<>("isLinkedToNext", false, this);

	final private static int MAX_DOT_COUNT = 3;
	final private static int PAUSE_POSITION = 3 * 7;

	// </editor-fold>

	public long keydownTimestamp;

	public Boolean isLongerThan(Note rival) { return getRealLength().compareTo(rival.getRealLength()) > 0; }

	// <editor-fold desc="implementing abstract model">

	@Override
	public int hashCode() {
		return (tune.get().byteValue() << (8 * 3) + limit(channel.get(), 0, 15) << (8 * 2 + 4)); // 1111 1111 1111 0000 0000 0000 0000 0000
	}

	@Override
	public boolean equals(Object rival) { 	// it's a bit arguable. this equals is supposed to be used only in context of one chord or Playback (two equal note-s cant sound simulatenously)
		return rival instanceof Note && ((Note)rival).tune.get() == this.tune.get() && ((Note)rival).channel.get() == this.channel.get();
	}

	@Override
	public Note reconstructFromJson(JSONObject dict) {
		super.reconstructFromJson(dict);
		/** @legacy */
		if (dict.has("numerator")) {
			this.setLength(new Fraction(dict.getInt("numerator"), 64));
		}
		return this;
	}

	// </editor-fold>

	// <editor-fold desc="getters">

	public int getTimeMilliseconds(int tempo) {
		return getTimeMilliseconds(getRealLength(), tempo);
	}

	// TODO: separate it into two parts. What we pass to SMF are not milliseconds, they are beats!
	// playback should be 1000 and what we show in status bar - too
	public static int getTimeMilliseconds(Fraction length, int tempo) {
		return getTimeUnits(length, tempo, 1000);
	}

	public static int getTimeBeats(Fraction length, int tempo) {
		// 960 is hardcoded, i don't completely understand what this number means
		return getTimeUnits(length, tempo, 960);
	}

	private static int getTimeUnits(Fraction length, int tempo, int unitsInMinute) {
		int minute = 60 * unitsInMinute;
		int semibreveTime = 4 * minute / tempo;
		return length.multiply(semibreveTime).intValue();
	}

	public int getDotCount() {

		Fraction length = this.length.get().divide(ImageStorage.getTallLimit().multiply(2)); // to make sure, that the length is less than 1
		// don't worry, denominator does not affect dot count

		return 31 - Integer.numberOfLeadingZeros(length.getNumerator() + 1) - 1; // 3/8 => 001_1_ + 1 = 0100 => 3 - 2 = 1 >D
	}

	private void setDotCount(int dotCount) {
		Fraction checkSum = getCleanLength();

		for (int i = 1; i <= dotCount; ++i) {
			checkSum = checkSum.add(getCleanLength().divide(pow(2, i)));
		}

		this.setLength(checkSum);
	}

	private int pow(int n, int e) {
		return e == 0 ? 1 : n * pow(n, e - 1);
	}

	public Fraction getCleanLength() { // i.e. length without dots: 1/4, 1/2
		return length.get().getDenominator() == 1
				? new Fraction(length.get().getNumerator() * 2, length.get().getDenominator() + 1)
				: new Fraction(length.get().getNumerator() + 1, length.get().getDenominator() * 2);
	}

	// </editor-fold>

	// <editor-fold desc="one-line-getters">

	// 0 - do, 2 - re, 4 - mi, 5 - fa, 7 - so, 9 - la, 10 - ti

	@Override
	public int ivoryIndex(KeySignature siga) { // siga - key signature from staff config
		if (isPause()) {
			return PAUSE_POSITION;
		} else {
			return INote.super.ivoryIndex(siga)/* - (isEbony() && isSharp.get() ? 1 : 0)*/; /** @debug, but i suppose, we should get rid of this */
		}
	}

	public Boolean isPause() { return tune.get() == 0; }

	// </editor-fold>

	// <editor-fold desc="model getters/setters">

	// TODO: we can use this.{field}.get instead of manually creating separate getters
	// model getters
	public Integer getTune() { return tune.get(); }
	public Integer getChannel() { return channel.get(); }
	public Fraction getLength() { return length.get(); }
	public Boolean isTriplet() { return isTriplet.get(); }
	public Boolean getIsSharp() { return isSharp.get(); }
	public Boolean getIsMuted() { return isMuted.get(); }
	public Boolean getIsLinkedToNext() { return isLinkedToNext.get(); }
	// model setters
	public Note setTune(int value) {
		this.tune.set(value);
		return this;
	}
	public Note setLength(Fraction value){ this.length.set(value); return this; }
	/** @Bug - note is immutable, this will blow with fatal !!! */
	public Note setChannel(int value) { this.channel.set(value); return this; }
	public Note setIsSharp(Boolean value) { this.isSharp.set(value); return this; }
	public Note setIsMuted(Boolean value) { this.isMuted.set(value); return this; }
	public Note setIsLinkedToNext(Boolean value) { this.isLinkedToNext.set(value); return this; }

	// </editor-fold>

	public Note setKeydownTimestamp(long value) { this.keydownTimestamp = value; return this; }

	// <editor-fold desc="event handles">

	public Note triggerIsSharp() {
		setIsSharp(!getIsSharp()); return this; }
	public Note triggerIsMuted() {
		setIsMuted(!getIsMuted()); return this; }
	public void triggerIsLinkedToNext() {
		isLinkedToNext.set(!isLinkedToNext.get());
	}
	public Note triggerTupletDenominator() {
		isTriplet.set(!isTriplet.get());
		return this;
	}

	public Note incLen() {
		setLength(new Fraction(length.get().getNumerator() * 2, length.get().getDenominator()));
		return this;
	}

	public Note decLen() {
		setLength(new Fraction(length.get().getNumerator(), length.get().getDenominator() * 2));
		return this;
	}

	public Boolean putDot() {
		if (getDotCount() < MAX_DOT_COUNT) {
			setDotCount(getDotCount() + 1);
			return true;
		} else {
			return false;
		}
	}

	public Boolean removeDot() {
		if (getDotCount() > 0) {
			setDotCount(getDotCount() - 1);
			return true;
		} else {
			return false;
		}
	}

	// </editor-fold>

	final public static int OCTAVA = 12;

	// small octava
	final public static int DO = 60;
	final public static int RE = 62;
	final public static int MI = 64;
	final public static int FA = 65;
	final public static int SO = 67;
	final public static int LA = 69; // 440 hz
	final public static int TI = 71;

	// private static methods
}