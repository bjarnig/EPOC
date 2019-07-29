
Beffect : Bobject
{ 	
	var <>wrap;
	
	*loadSynthDefs {
		
	 	var children;
		children = Beffect.subclasses;
		children.do{|item| ('Beffect.loadSynthDefs: ' ++ item).postln; 
		item.loadSynthDefs};
	}
	
	get{ |key|
		^wrap.get(key);
	}
	
	getArgNames {
		^wrap.argNames;
	}
	
	getArgValues {
		^wrap.argValues;
	}
	
	set{ |key, value|
		wrap.set(key, value);
	}
	
	setValues{ |values|
		wrap.setValues(values);	
	}
	
	map{ |key, value|
		wrap.synth.map(key, value);
	}
	
	unMap{ |key, value|
		wrap.synth.unMap(key, value);
	}
	
	play 
	{
	}
}