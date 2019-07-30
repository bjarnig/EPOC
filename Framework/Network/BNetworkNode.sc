BNetworkNode
{
	var <>duration;
	var <>items;
	var <>nextNode;
	var <>name;
	var <>durationOffset;
	var <>startOffset;
	var <>controlOffset;
	var <>playType;
	var <>start;
	var <>nextNodes;
	var scheduler;
	var playRoutine;

	*new {|duration, items, nextNode, name, durationOffset=1, startOffset=1, controlOffset, playType=1, start=0, nextNodes|
		^super.newCopyArgs(duration, items, nextNode, name, durationOffset, startOffset, controlOffset, playType, start, nextNodes).init();
	}

	init {
		start = 0;
		scheduler = TempoClock(1);
	}

	playFunction {
		^{this.play};
	}

	stopFunction {
		^{this.stop};
	}

	play {arg dispose=1;

	if(this.playType == 2, { this.sortByTimbralWeight.value });
	if(this.playType == 3, { this.sortByTemporalWeight.value });
	if(this.playType == 4, { this.sortBySumWeight.value });
	if(this.playType == 5, { this.sortByDiffWeight.value });

	this.runNode.value;

	if(nextNode.notNil, {

	Routine {
	'waiting next node'.postln;
	duration.wait;
	'play next node'.postln;
	this.nextNode.play;
	}.play(AppClock);

	}, {

	});

	}

	runNode {arg dispose=1;
	var totalTime=0;

	this.items.do({arg item; if((item.start + item.duration) > totalTime, {totalTime = item.start + item.duration})});
	('total node time : ' ++ totalTime).postln;

	this.items.do({arg item; item.duration = item.duration *  this.durationOffset});
	// if(this.controlOffset.notNil, {this.items.do({arg item; item.control = item.initialControl.getOffsetMultiply(this.controlOffset); item.update})}); // PRE APRIL 2012

	if(this.controlOffset.notNil, {this.items.do({arg item;

		if(item.initialControl.isNil, {item.initialControl = item.control.deepCopy});
		item.control = BControl.new;
		item.control = item.initialControl.deepCopy;
		item.control.offsetMultiply(this.controlOffset.deepCopy);
		})
	});

	playRoutine =  Routine.new {

	1.do({arg a;
	this.items.do({arg item; item.initObject});
	0.5.wait; // Remove ?

	this.items.do({arg item; scheduler.sched(item.start * this.startOffset, item.object.playFunction); // add time offset
	item.envelopes.do({arg e; scheduler.sched(e.start + item.start, e.runEnvelopeFunction)})});
	(totalTime + 2).wait;

	// if(dispose > 0, {this.items.do({arg item; item.dispose})});
	this.items.do({arg item; item.dispose});
	'node stopped'.postln;

	})};

	playRoutine.play(AppClock);

	}

	update {|mode=1|

	this.items.do({arg item;
	if(this.controlOffset.notNil, {

	if(mode > 0, {

	if(item.initialControl.isNil, {item.initialControl = item.control.deepCopy});
	// item.control = item.initialControl.offsetMultiply(this.controlOffset);  // PRE-APRIL 2012
	  item.control = BControl.new;
	  item.control = item.initialControl.deepCopy;
	  item.control.offsetMultiply(this.controlOffset.deepCopy);

	},{
	item.control = this.controlOffset.deepCopy;
	});

	item.update(updateValues:1);

	})})

	}

	stop {
	'inside stop ?????'.postln;
	playRoutine.stop;
	scheduler.clear;
	this.items.do({arg item; item.stop});
	this.items.do({arg item; item.dispose});

	}

	sortByStart {
	this.items = this.items.sort({ arg a, b; a.start < b.start });
	}

	sortByTimbralWeight {
	var transferValues, transferCount=0, transferObj;

	this.sortByStart.value;
	transferValues = ();

	this.items.do({arg item; transferValues.put(transferCount, [item.start, item.duration, item.control.amplitude, item.control.attack, item.control.release]);
 	transferCount = transferCount + 1});
 	transferCount = 0;

	this.items = this.items.sort({ arg a, b; a.control.weightTimbral > b.control.weightTimbral });
	this.items.do({arg item; transferObj = transferValues.at(transferCount);
	item.start = transferObj[0];
	item.duration = transferObj[1];
	item.control.amplitude = transferObj[2];
	item.control.attack = transferObj[3];
	item.control.release = transferObj[4];
	transferCount = transferCount + 1});
	}

	sortByTemporalWeight {
	var transferValues, transferCount=0, transferObj;

	this.sortByStart.value;
	transferValues = ();

	this.items.do({arg item; transferValues.put(transferCount, [item.start, item.duration, item.control.amplitude, item.control.attack, item.control.release]);
 	transferCount = transferCount + 1});
 	transferCount = 0;

	this.items = this.items.sort({ arg a, b; a.control.weightTemporal > b.control.weightTemporal });
	this.items.do({arg item; transferObj = transferValues.at(transferCount);
	item.start = transferObj[0];
	item.duration = transferObj[1];
	item.control.amplitude = transferObj[2];
	item.control.attack = transferObj[3];
	item.control.release = transferObj[4];
	transferCount = transferCount + 1});
	}

	sortBySumWeight {
	var transferValues, transferCount=0, transferObj;

	this.sortByStart.value;
	transferValues = ();

	this.items.do({arg item; transferValues.put(transferCount, [item.start, item.duration, item.control.amplitude, item.control.attack, item.control.release]);
 	transferCount = transferCount + 1});
 	transferCount = 0;

	this.items = this.items.sort({ arg a, b; a.control.weightSum > b.control.weightSum });
	this.items.do({arg item; transferObj = transferValues.at(transferCount);
	item.start = transferObj[0];
	item.duration = transferObj[1];
	item.control.amplitude = transferObj[2];
	item.control.attack = transferObj[3];
	item.control.release = transferObj[4];
	transferCount = transferCount + 1});
	}

	sortByDiffWeight {
	var transferValues, transferCount=0, transferObj;

	this.sortByStart.value;
	transferValues = ();

	this.items.do({arg item; transferValues.put(transferCount, [item.start, item.duration, item.control.amplitude, item.control.attack, item.control.release]);
 	transferCount = transferCount + 1});
 	transferCount = 0;

	this.items = this.items.sort({ arg a, b; a.control.weightDiff > b.control.weightDiff });
	this.items.do({arg item; transferObj = transferValues.at(transferCount);
	item.start = transferObj[0];
	item.duration = transferObj[1];
	item.control.amplitude = transferObj[2];
	item.control.attack = transferObj[3];
	item.control.release = transferObj[4];
	transferCount = transferCount + 1});
	}
}
