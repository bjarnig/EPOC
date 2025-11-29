
BSeq : BItem
{
	*loadSynthDefs {
	var children;
	children = BSeq.subclasses;
	children.do{|item| ('BSeq.loadSynthDefs: ' ++ item).postln;
	item.loadSynthDefs};
	}

	play
	{
		('BSeq.play (CHILD HAS NOT IMPLEMENTED)').postln;
	}

	playDuration
	{|pat|

		Routine
		{
			1.do({arg b;
			var rp = pat.play;
			duration.wait;
			rp.stop;
		})
		}.play;
	}

	playFunction {
		^{this.play};
	}
}
