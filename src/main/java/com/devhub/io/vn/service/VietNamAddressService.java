package com.devhub.io.vn.service;

import security.license.RequiresLicense;
import security.license.RequiresLicense.LicenseLevel;

@RequiresLicense(
	    feature = "Viet Nam Address Service",
	    level = LicenseLevel.PREMIUM,
	    strict = true,
	    message = "VietNamAddressService requires a Premium license. Contact support@devhub.io.vn"
	)
public class VietNamAddressService {

}
