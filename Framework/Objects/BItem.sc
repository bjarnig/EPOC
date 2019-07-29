BItem
{
	var <>id, <description, <>duration, <>control, <>outBus;
		
	*load {
	var children;
	children = BItem.subclasses;
	children.do{|item| ('BItem.load: ' ++ item).postln; 
	item.loadSynthDefs};
	}	
	
	update {
	('BItem.Update (CHILD HAS NOT IMPLEMENTED)').postln; 
	}
	
	setParamAndUpdate {|param, value|
	control.setParamValue(param, value);
	this.update.value;
	}
}
