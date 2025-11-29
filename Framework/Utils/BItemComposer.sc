BItemComposer
{
	*tendancyMask { arg delta=0.115, dur=100, lower=[0.1, 0.8, 0.1], upper=[0.2, 0.9, 0.5], durations=[2, 10, 0], param="frequency", item;

	var lineA, lineB, number, iterations;

	lineA = Pseg(
			Pseq(lower, inf),
			Pseq(durations, inf)
			).asStream;

	lineB = Pseg(
			Pseq(upper, inf),
			Pseq(durations, inf)
			).asStream;

	item.play;
	iterations = dur/delta;

	Routine({
		iterations.do({

		number = rrand(lineA.next, lineB.next);
		item.setParamAndUpdate(param, number);
		("SET " ++ param ++ ": " ++ number).postln;
		delta.wait
		})
		}).play
	}

	*interpolate { arg delta=0.115, dur=100, itemA, itemB;

	var speedAmt, densityAmt, frequencyAmt, entropyAmt, amplitudeAmt, colorAmt, surfaceAmt, locationAmt, positionAmt;
	var iterations;

	iterations = dur/delta;
	speedAmt = (itemB.control.speed - itemA.control.speed) / iterations;
	densityAmt = (itemB.control.density - itemA.control.density) / iterations;
	frequencyAmt = (itemB.control.frequency - itemA.control.frequency) / iterations;
	entropyAmt = (itemB.control.entropy - itemA.control.entropy) / iterations;
	amplitudeAmt = (itemB.control.amplitude - itemA.control.amplitude) / iterations;
	colorAmt = (itemB.control.color - itemA.control.color) / iterations;
	surfaceAmt = (itemB.control.surface - itemA.control.surface) / iterations;
	locationAmt = (itemB.control.location - itemA.control.location) / iterations;
	positionAmt = (itemB.control.position - itemA.control.position) / iterations;

	itemA.play;

	Routine({

		iterations.do({
		itemA.control.speed = itemA.control.speed + speedAmt;
		itemA.control.frequency = itemA.control.frequency + frequencyAmt;
		itemA.control.density = itemA.control.frequency + frequencyAmt;
		itemA.control.entropy = itemA.control.entropy + entropyAmt;
		itemA.control.amplitude = itemA.control.amplitude + amplitudeAmt;
		itemA.control.color = itemA.control.color + colorAmt;
		itemA.control.surface = itemA.control.surface + locationAmt;
		itemA.control.location = itemA.control.location + locationAmt;
		itemA.control.position = itemA.control.position + positionAmt;
		itemA.update;
		delta.wait

		})
		}).play
	}

	*sequence {arg item, durations=[1], densities=[], frequencies=[], speeds=[];

	var patDurations, patDensities, patFrequencies, patSpeeds;

	if(densities == [], {densities = [item.control.density]});
	if(frequencies == [], {frequencies = [item.control.frequency]});
	if(speeds == [], {speeds = [item.control.speed]});

	patDurations = Pseq(durations, inf).asStream;
	patFrequencies = Pseq(frequencies, inf).asStream;
	patDensities = Pseq(densities, inf).asStream;
	patSpeeds = Pseq(speeds, inf).asStream;

	Routine({
		inf.do({var newItem;

		newItem = item;
		// ('FIRST STEP').postln;
		newItem.duration = patDurations.next;

		newItem.control.frequency = patFrequencies.next;
		newItem.control.density = patDensities.next;
		newItem.control.speed = patSpeeds.next;
		('FREQ: ' ++ newItem.control.frequency).postln;
		('DURATION: ' ++ newItem.duration).postln;
		newItem.play;
		newItem.update;

		(item.duration * 1.1).wait;
		// ('LAST STEP').postln;

		})
	}).play

	}

	*sequencePatterns {arg item, deltaFrom=2, deltaTo=6, patDensities, patFrequencies, patSpeeds;

	item.play;

	Routine({
		inf.do({

		item.control.frequency = patFrequencies.next;
		item.control.density = patDensities.next;
		item.control.speed = patSpeeds.next;
		item.update;

		('FREQ: ' ++ item.control.frequency).postln;
		('DENSITY: ' ++ item.control.density).postln;
		('SPEED: ' ++ item.control.speed).postln;

		rrand(deltaFrom, deltaTo).wait;

		})
	}).play

	}

	*exploreStates {arg itemLists, duration=25, offset=0;

	Routine({
		1.do({

			itemLists.do({ arg item;

				item.do({ arg item;
					item.play;
				});

				(duration+offset).wait;
			});
		})
	}).play

	}

	*relate {arg item1, item2, deltaFrom=2, deltaTo=6, param="amplitude", from=0.4, to=0.6, mode=1;
	var value1, value2;
	var range = to - from;

	Routine({

	item1.initObject;
	item2.initObject;
	(0.2).wait;
	item1.object.play;
	item2.object.play;

		inf.do({

		value1 = rrand(from, to);
		item1.object.setParamAndUpdate(param, value1);

		if(mode == 1, {value2 = (to - value1)});
		item2.object.setParamAndUpdate(param, value2);

		('value1: ' ++ value1).postln;
		('value2: ' ++ value2).postln;

		rrand(deltaFrom, deltaTo).wait;

		})
	}).play

	}
}
