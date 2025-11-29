Bobject
{
	var <description;

	*load {
	var children;
	children = Bobject.subclasses;
	children.do{|item| ('Bobject.loadSynthDefs: ' ++ item).postln;
	item.loadSynthDefs};
	}
}

