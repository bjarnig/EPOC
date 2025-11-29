
Brout : Bobject
{
	*loadSynthDefs {

		var children;
		children = Brout.subclasses;
		children.do{|item| ('Brout.loadSynthDefs: ' ++ item).postln;
		item.loadSynthDefs};
	}

	play
	{
	}
}
