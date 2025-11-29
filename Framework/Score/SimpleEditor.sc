SimpleEditor{
	var <value;

	*new{ arg value;
		^super.newCopyArgs(value)
	}

	value_{ |v,change=true|
		value = v;
		if(change,{ this.changed });
	}

	setUnmappedValue{  arg v, change = true;
		this.value_(v,change);
	}

	unmappedValue{
		^value
	}

}

