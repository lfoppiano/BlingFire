/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */


#include "FAConfig.h"
#include "FAUtils.h"
#include "FAException.h"

#include <iostream>
#include <string>
#include <fstream>

#ifndef STATUS_SUCCESS
#define STATUS_SUCCESS (0x00000000L)
#endif

using namespace BlingFire;

const char * __PROG__ = "";

const char * g_pInFile = NULL;
const char * g_pOutFile = NULL;
const char * g_pName = "g_dump";
const char * g_pSizeName = NULL;

std::ostream * g_pOs = &std::cout;
std::ofstream g_ofs;


void usage ()
{
  std::cout << "\n\
Usage: fa_dump2cpp [OPTIONS]\n\
\n\
This tool prints a binary file as a C array data structure.\n\
\n\
  --in=<input> - input file, if omited stdin is used\n\
\n\
  --out=<output> - writes output to the <output> file,\n\
    if omited stdout is used\n\
\n\
  --name=<name> - C array name, g_dump is used by default\n\
\n\
  --size-name=<name> - Uncompressed size name, unspecified by default\n\
\n\
  --compress - compress data using RtlCompressBuffer\n\
\n\
";
}


void process_args (int& argc, char**& argv)
{
    for (; argc--; ++argv) {

        if (!strcmp ("--help", *argv)) {
            usage ();
            exit (0);
        }
        if (0 == strncmp ("--in=", *argv, 5)) {
            g_pInFile = &((*argv) [5]);
            continue;
        }
        if (0 == strncmp ("--out=", *argv, 6)) {
            g_pOutFile = &((*argv) [6]);
            continue;
        }
        if (0 == strncmp ("--name=", *argv, 7)) {
            g_pName = &((*argv) [7]);
            continue;
        }
        if (0 == strncmp ("--size-name=", *argv, 12)) {
            g_pSizeName = &((*argv) [12]);
            continue;
        }
    }
}


int __cdecl main (int argc, char ** argv)
{
    __PROG__ = argv [0];

    --argc, ++argv;

    ::FAIOSetup ();

    // process command line
    process_args (argc, argv);

    try {

        if (g_pOutFile) {
            g_ofs.open (g_pOutFile, std::ios::out);
            g_pOs = &g_ofs;
        }

        FILE * file = NULL;
        int res = fopen_s (&file, g_pInFile, "rb");
        LogAssert (0 == res && NULL != file);

        res = fseek (file, 0, SEEK_END);
        LogAssert (0 == res);

        unsigned int Size = (unsigned int)ftell (file);
        LogAssert (0 < Size);

        unsigned int UncompressedSize = Size;

        res = fseek (file, 0, SEEK_SET);
        LogAssert (0 == res);

        unsigned char * pImageDump = new unsigned char [Size];
        DebugLogAssert (pImageDump);

        const size_t ActSize = fread (pImageDump, sizeof (char), Size, file);
        LogAssert (ActSize == Size);

        fclose (file);

        // write the header
        (*g_pOs) 
            << std::endl << "//" << std::endl
            << "// Autogenerated file with" << std::endl
            << "//     fa_dump2cpp --in=\"" << g_pInFile << "\" --name=" << g_pName;
        
        if (NULL != g_pSizeName)
        {
            (*g_pOs) << std::endl << "//         --size-name=" << g_pSizeName << std::endl;
        }

        (*g_pOs) << std::endl;

        (*g_pOs)
            << "//" << std::endl << std::endl;

        if (NULL != g_pSizeName)
        {
            (*g_pOs)
                << "static const unsigned int " << g_pSizeName << " = " << UncompressedSize << ";"
                << std::endl << std::endl;
        }

        (*g_pOs)
            << "static const unsigned char " << g_pName << "[" << Size << "] ="
            << std::endl << "{";

        // write bytes
        for (unsigned i = 0; i < Size; ++i) {

            if (0 == i % 16) {
                (*g_pOs) << std::endl << "    ";
            }

            (*g_pOs) << (unsigned int) (pImageDump [i]);

            if (i + 1 != Size) {
                (*g_pOs) << ", ";
            }
        }

        // write footer
        (*g_pOs) << std::endl << "};" << std::endl << std::endl;

        delete [] pImageDump;
        pImageDump = NULL;

    } catch (const FAException & e) {

        const char * const pErrMsg = e.GetErrMsg ();
        const char * const pFile = e.GetSourceName ();
        const int Line = e.GetSourceLine ();

        std::cerr << "ERROR: " << pErrMsg << " in " << pFile \
            << " at line " << Line << " in program " << __PROG__ << '\n';

        return 2;

    } catch (...) {

        std::cerr << "ERROR: Unknown error in program " << __PROG__ << '\n';

        return 1;
    }

    return 0;
}
