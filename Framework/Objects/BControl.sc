
BControl
{
	var <>shape=0, <>speed, <>density, <>frequency, <>entropy, <>amplitude, <>color, <>surface, <>location, <>position, <>attack, <>release;

		*new { |shape=0, speed=0.5, density=0.5, frequency=0.5, entropy=0.5, amplitude=0.5, color=0.01, surface=0.5, location=0.5, position=0.5, attack=0.001, release=0.001|
		^super.newCopyArgs(shape, speed, density, frequency, entropy, amplitude, color, surface, location, position, attack, release);
	}

	addSpeed { arg value;
	if(value != nil, {speed = speed + value}, {speed = speed + BConstants.control_interval});
	}

	addDensity { arg value;
	if(value != nil, {density = density + value}, {density = density + BConstants.control_interval});
	}

	addFrequency { arg value;
	if(value != nil, {frequency = frequency + value}, {frequency = frequency + BConstants.control_interval});
	}

	addEntropy { arg value;
	if(value != nil, {entropy = entropy + value}, {entropy = entropy + BConstants.control_interval});
	}

	addAmplitude { arg value;
	if(value != nil, {amplitude = amplitude + value}, {amplitude = amplitude + BConstants.control_interval});
	}

	addColor { arg value;
	if(value != nil, {color = color + value}, {color = color + BConstants.control_interval});
	}

	addSurface { arg value;
	if(value != nil, {surface = surface + value}, {surface = surface + BConstants.control_interval});
	}

	addLocation { arg value;
	if(value != nil, {location = location + value}, {location = location + BConstants.control_interval});
	}

	addPosition { arg value;
	if(value != nil, {position = position + value}, {position = position + BConstants.control_interval});
	}

	addAttack { arg value;
	if(value != nil, {attack = attack + value}, {attack = attack + BConstants.control_interval});
	}

	addRelease { arg value;
	if(value != nil, {release = release + value}, {release = release + BConstants.control_interval});
	}

	subSpeed { arg value;
	if(value != nil, {speed = speed - value}, {speed = speed - BConstants.control_interval});
	}

	subDensity { arg value;
	if(value != nil, {density = density - value}, {density = density - BConstants.control_interval});
	}

	subFrequency { arg value;
	if(value != nil, {frequency = frequency - value}, {frequency = frequency - BConstants.control_interval});
	}

	subEntropy { arg value;
	if(value != nil, {entropy = entropy - value}, {entropy = entropy - BConstants.control_interval});
	}

	subAmplitude { arg value;
	if(value != nil, {amplitude = amplitude - value}, {amplitude = amplitude - BConstants.control_interval});
	}

	subColor { arg value;
	if(value != nil, {color = color - value}, {color = color - BConstants.control_interval});
	}

	subSurface { arg value;
	if(value != nil, {surface = surface - value}, {surface = surface - BConstants.control_interval});
	}

	subLocation { arg value;
	if(value != nil, {location = location - value}, {location = location - BConstants.control_interval});
	}

	subPosition { arg value;
	if(value != nil, {position = position - value}, {position = position - BConstants.control_interval});
	}

	subAttack { arg value;
	if(value != nil, {attack = attack - value}, {attack = attack - BConstants.control_interval});
	}

	subRelease { arg value;
	if(value != nil, {release = release - value}, {release = release - BConstants.control_interval});
	}

	setParamValue { arg param, value;

	if(param == "speed", {speed = value; });
	if(param == "density", {density = value; });
	if(param == "frequency", {frequency = value; });
	if(param == "entropy", {entropy = value; });
	if(param == "amplitude", {amplitude = value; });
	if(param == "color", {color = value; });
	if(param == "surface", {surface = value; });
	if(param == "location", {location = value; });
	if(param == "position", {position = value; });
	if(param == "attack", {attack = value; });
	if(param == "release", {release = value; });

	}

	setAttributesInRange {

		if(speed > 0.999, {speed = 0.99});
		if(density > 0.999, { density = 0.99 });
		if(frequency > 0.999, { frequency = 0.99 });
		if(entropy > 0.999, { entropy = 0.99 });
		if(amplitude > 0.999, { amplitude = 0.99 });
		if(color > 0.999, { color = 0.99 });
		if(surface > 0.999, { surface = 0.99 });
		if(location > 0.999, { location = 0.99 });
		if(position > 0.999, { position = 0.99 });
		if(attack > 0.999, { attack = 0.99 });
		if(release > 0.999, { release = 0.99 });
		if(speed < 0.001, {speed = 0.001});
		if(density < 0.001, { density = 0.001});
		if(frequency < 0.001, { frequency = 0.001});
		if(entropy < 0.0, { entropy = 0.0});
		if(amplitude < 0.001, { amplitude = 0.001});
		if(color < 0.0, { color = 0.0});
		if(surface < 0.001, { surface = 0.001});
		if(location < 0.001, { location = 0.001});
		if(position < 0.001, { position = 0.001});
		if(attack < 0.001, { attack = 0.001});
		if(release < 0.001, { release = 0.001});
		if(attack + release > 1.0, { release = 1 - attack});

	}

	randomize {

		speed = rrand(0.001, 0.999);
	    	density = rrand(0.001, 0.999);
	    	frequency = rrand(0.001, 0.999);
	    	amplitude = rrand(0.6, 0.999);
	    	entropy = rrand(0.001, 0.999);
	    	color = rrand(0.001, 0.999);
	    	surface = rrand(0.001, 0.999);
	    	location = rrand(0.001, 0.999);
	    	position = rrand(0.001, 0.999);
	}

	copy {arg control;

		this.speed = control.speed;
	    	this.density = control.density;
	    	this.frequency = control.frequency;
	    	this.amplitude = control.amplitude;
	    	this.entropy = control.entropy;
	    	this.color = control.color;
	    	this.surface = control.surface;
	    	this.location = control.location;
	    	this.position = control.position;
	    	this.attack = control.attack;
	    	this.release = control.release;
	}

	offsetMultiply {arg control, add=0.5;

		this.speed = (this.speed * (control.speed + add)).min(1.0).max(0.0);
	   	this.density = (this.density * (control.density + add)).min(1.0).max(0.0);
	    	this.frequency = (this.frequency * (control.frequency + add)).min(1.0).max(0.0);
	    	this.amplitude = (this.amplitude * (control.amplitude + add)).min(1.0).max(0.0);
	    	this.entropy = (this.entropy * (control.entropy + add)).min(1.0).max(0.0);
	    	this.color = (this.color * (control.color + add)).min(1.0).max(0.0);
	    	this.surface = (this.surface * (control.surface + add)).min(1.0).max(0.0);
	    	this.location = (this.location * (control.location + add)).min(1.0).max(0.0);
	    	this.position = (this.position * (control.position + add)).min(1.0).max(0.0);
	    	this.attack = (this.attack * (control.attack + add)).min(1.0).max(0.0);
	    	this.release = (this.release * (control.release + add)).min(1.0).max(0.0);
	}

	getOffsetMultiply {arg control, add=0.5;
	var newControl;
	'inside get offset multiply'.postln;

		newControl = BControl.new;
		newControl.speed = (this.speed * (control.speed + add)).min(1.0).max(0.0);
	   	newControl.density = (this.density * (control.density + add)).min(1.0).max(0.0);
	    	newControl.frequency = (this.frequency * (control.frequency + add)).min(1.0).max(0.0);
	    	newControl.amplitude = (this.amplitude * (control.amplitude + add)).min(1.0).max(0.0);
	    	newControl.entropy = (this.entropy * (control.entropy + add)).min(1.0).max(0.0);
	    	newControl.color = (this.color * (control.color + add)).min(1.0).max(0.0);
	    	newControl.surface = (this.surface * (control.surface + add)).min(1.0).max(0.0);
	    	newControl.location = (this.location * (control.location + add)).min(1.0).max(0.0);
	    	newControl.position = (this.position * (control.position + add)).min(1.0).max(0.0);
	    	newControl.attack = (this.attack * (control.attack + add)).min(1.0).max(0.0);
	    	newControl.release = (this.release * (control.release + add)).min(1.0).max(0.0);
		^newControl;
	}

	postln {

		("speed:" ++ speed.round(0.01) ++ " density:" ++ density.round(0.01) ++ " frequency:" ++ frequency.round(0.01) ++ " amplitude:" ++ amplitude.round(0.01) ++ " entropy:" ++ entropy.round(0.01) ++ " color:" ++ color.round(0.01) ++ " surface:" ++ surface.round(0.01) ++ " location:" ++ location.round(0.01) ++ " position:" ++ position.round(0.01) ++ " attack:" ++ attack ++ " release:" ++ release).postln;

	}

	text {

	^("BControl.new(speed:" ++ speed.round(0.0001) ++ ", density:" ++ density.round(0.0001) ++ ", frequency:" ++ frequency.round(0.0001) ++ ", amplitude:" ++ amplitude.round(0.0001) ++ ", entropy:" ++ entropy.round(0.0001) ++ ", color:" ++ color.round(0.0001) ++ ", surface:" ++ surface.round(0.0001) ++ ", location:" ++ location.round(0.0001) ++ ", position:" ++ position.round(0.0001) ++ ", attack:" ++ attack ++ ", release:" ++ release ++ ")")

	}

	print {

	this.text.value.postln;

	}

	weightSum {

	^speed + density + frequency + amplitude + entropy + color + surface + location + position;

	}

	weightDiff {

	^((speed - density) + (frequency - amplitude) + (entropy - position) + (color - surface));

	}

	weightTemporal {

	^((speed + density + entropy) - ((frequency + color - surface) * 0.5));

	}

	weightTimbral {

	^((frequency + color - surface) - ((speed + density + entropy) * 0.5));

	}
}
