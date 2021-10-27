#!/usr/bin/perl -w
use strict;
use warnings;

# palce to find the .html files
my $doc_root = "./doc-root";

# the output file
my $gen_file = "./request-patterns/requests.txt";

my $DEBUG = 1;

# build list of files available

my @files = <$doc_root/*.html>;

my $n = 1000;

open(MYOUTFILE, ">$gen_file");

for (my $i = 1; $i < $n; $i++)
{
  my $r = rand();
	
  print "--Picking $i\n" if ($DEBUG);
  print "  Generate $r\n" if ($DEBUG);

  # find the file to generate
  my $index = int($r * $#files);

  my $file = $files[$index];

  my ($dot, $dir, $filen) = split(/\//, $file);
  print "  Pick $filen\n" if ($DEBUG);
  print MYOUTFILE "$filen\n"
}

close(MYOUTFILE);
