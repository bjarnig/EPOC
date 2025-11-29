
BGen : BItem
{
	var <>wrap;

	*loadSynthDefs {
	var children;
	children = BGen.subclasses;
	children.do{|item| ('BGen.loadSynthDefs: ' ++ item).postln;
	item.loadSynthDefs};
	}

	play
	{
		('BGen.play (CHILD HAS NOT IMPLEMENTED)').postln;
	}

	playDuration
	{
		('BGen.playDuration (CHILD HAS NOT IMPLEMENTED)').postln;
	}

	playFunction {
		^{this.play};
	}
}
