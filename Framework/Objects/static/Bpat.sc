
Bpat : Bobject
{
	var <>duration = inf;

	*loadSynthDefs {

		var children;
		children = Bpat.subclasses;
		children.do{|item| ('Bpat.loadSynthDefs: ' ++ item).postln;
		item.loadSynthDefs};
	}

	play
	{
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
}
