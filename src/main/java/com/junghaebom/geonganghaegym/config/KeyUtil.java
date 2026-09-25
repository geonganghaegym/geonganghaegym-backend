package com.junghaebom.geonganghaegym.config;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class KeyUtil {
	public static ECPrivateKey getPrivateKeyFromBytes(byte[] keyBytes) throws InvalidKeySpecException {
		try {
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("EC");
			PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
			return (ECPrivateKey)privateKey;
		} catch (Exception e) {
			throw new InvalidKeySpecException("Failed to create ECPrivateKey from bytes", e);
		}
	}
}
