/**
 * Most endpoints in Apperta require a JWT Token to determine who the user is,
 * and their corresponding privileges.
 *
 * Apperta provides a built in OAuth Server that provides JWT tokens for users
 * registered with Apperta directly. This is in the server package
 *
 * The client package manages the process of receiving a JWT token, determining
 * if the user can access Apperta, and then populating the AppertaPrinciple with
 * user information and priviledges.
 *
 * The client will only accept JWT tokens from known OAuth Servers. Currently, the
 * only known OAuth Server is the one provided by Apperta. To validate Apperta produced
 * JWT tokens, a symettric key is used (defined by apperta.oauth.jwt.symetric.key in
 * application.properties)
 */
package com.staircase13.apperta.auth;