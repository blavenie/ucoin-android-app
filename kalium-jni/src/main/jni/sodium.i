/* example.i */
 %module Sodium
%include "typemaps.i"
%include "stdint.i"
%include "arrays_java.i"
%include "carrays.i"
%include "various.i"

%apply int {unsigned long long};
%apply long[] {unsigned long long *};


%typemap(jni) unsigned char *"jbyteArray"
%typemap(jtype) unsigned char *"byte[]"
%typemap(jstype) unsigned char *"byte[]"
%typemap(in) unsigned char *{
    $1 = (unsigned char *) JCALL2(GetByteArrayElements, jenv, $input, 0);
}

%typemap(argout) unsigned char *{
    JCALL3(ReleaseByteArrayElements, jenv, $input, (jbyte *) $1, 0);
}

%typemap(javain) unsigned char *"$javainput"

/* Prevent default freearg typemap from being used */
%typemap(freearg) unsigned char *""



/* char types */
%typemap(jni) char *BYTE "jbyteArray"
%typemap(jtype) char *BYTE "byte[]"
%typemap(jstype) char *BYTE "byte[]"
%typemap(in) char *BYTE {
    $1 = (char *) JCALL2(GetByteArrayElements, jenv, $input, 0);
}

%typemap(argout) char *BYTE {
    JCALL3(ReleaseByteArrayElements, jenv, $input, (jbyte *) $1, 0);
}

%typemap(javain) char *BYTE "$javainput"

/* Prevent default freearg typemap from being used */
%typemap(freearg) char *BYTE ""







/* ***************************** */


/*
%typemap(jni) unsigned char*        "jbyteArray"
%typemap(jtype) unsigned char*      "byte[]"
%typemap(jstype) unsigned char*     "byte[]"
*/
 
%{
 /* Put header files here or function declarations like below */
#include "sodium.h"

 %}
const char *sodium_version_string(void);

int crypto_aead_chacha20poly1305_encrypt(unsigned char *c,
                                         unsigned long long *clen,
                                         const unsigned char *m,
                                         unsigned long long mlen,
                                         const unsigned char *ad,
                                         unsigned long long adlen,
                                         const unsigned char *nsec,
                                         const unsigned char *npub,
                                         const unsigned char *k);

int crypto_aead_chacha20poly1305_decrypt(unsigned char *m,
                                         unsigned long long *mlen,
                                         unsigned char *nsec,
                                         const unsigned char *c,
                                         unsigned long long clen,
                                         const unsigned char *ad,
                                         unsigned long long adlen,
                                         const unsigned char *npub,
                                         const unsigned char *k);

int crypto_hash_sha256(unsigned char *out, const unsigned char *in,
                       unsigned long long inlen);

int crypto_hash_sha512(unsigned char *out, const unsigned char *in,
                       unsigned long long inlen);

int crypto_generichash_blake2b(unsigned char *out, size_t outlen,
                               const unsigned char *in,
                               unsigned long long inlen,
                               const unsigned char *key, size_t keylen);
                           
int crypto_pwhash_scryptsalsa208sha256(unsigned char * const out,
                                    unsigned long long outlen,
                                    const char * const passwd,
                                    unsigned long long passwdlen,
                                    const unsigned char * const salt,
                                    unsigned long long opslimit,
                                    size_t memlimit);

int crypto_box_curve25519xsalsa20poly1305_keypair(unsigned char *pk,
                                                  unsigned char *sk);

void        randombytes(unsigned char *buf, unsigned long long size);
/* void        randombytes(unsigned char * const buf, const unsigned long long buf_len); */


int crypto_box_curve25519xsalsa20poly1305(unsigned char *c,
                                          const unsigned char *m,
                                          unsigned long long mlen,
                                          const unsigned char *n,
                                          const unsigned char *pk,
                                          const unsigned char *sk);


int crypto_box_curve25519xsalsa20poly1305_open(unsigned char *m,
                                               const unsigned char *c,
                                               unsigned long long clen,
                                               const unsigned char *n,
                                               const unsigned char *pk,
                                               const unsigned char *sk);


int crypto_scalarmult_curve25519(unsigned char *q, const unsigned char *n,
                                 const unsigned char *p);


int crypto_secretbox_xsalsa20poly1305(unsigned char *c,
                                      const unsigned char *m,
                                      unsigned long long mlen,
                                      const unsigned char *n,
                                      const unsigned char *k);

int crypto_secretbox_xsalsa20poly1305_open(unsigned char *m,
                                           const unsigned char *c,
                                           unsigned long long clen,
                                           const unsigned char *n,
                                           const unsigned char *k);

int crypto_sign_ed25519_seed_keypair(unsigned char *pk, unsigned char *sk,
                                     const unsigned char *seed);

int crypto_sign_ed25519(unsigned char *sm, unsigned long long *smlen,
                        const unsigned char *m, unsigned long long mlen,
                        const unsigned char *sk);

int crypto_sign_ed25519_open(unsigned char *m, unsigned long long *mlen,
                             const unsigned char *sm, unsigned long long smlen,
                             const unsigned char *pk);
                             
                             
int crypto_stream_xsalsa20(unsigned char *c, unsigned long long clen,
              const unsigned char *n, const unsigned char *k);


int crypto_stream_xsalsa20_xor(unsigned char *c, const unsigned char *m,
                  unsigned long long mlen, const unsigned char *n,
                  const unsigned char *k);
