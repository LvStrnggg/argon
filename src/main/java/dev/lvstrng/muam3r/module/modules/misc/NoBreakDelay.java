package dev.lvstrng.muam3r.module.modules.misc;

import dev.lvstrng.muam3r.module.Category;
import dev.lvstrng.muam3r.module.Module;
import dev.lvstrng.muam3r.utils.EncryptedString;

public final class NoBreakDelay extends Module {
	public NoBreakDelay() {
		super(EncryptedString.of("No Break Delay"),
				EncryptedString.of("Removes the break delay from mining blocks"),
				-1,
				Category.MISC);
	}
}
