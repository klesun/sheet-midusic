package org.shmidusic.stuff.Midi;

import org.apache.commons.math3.fraction.Fraction;
import org.shmidusic.stuff.tools.jmusic_integration.INota;

public interface IMidiScheduler
{
	void addNoteTask(Fraction when, INota nota);
}