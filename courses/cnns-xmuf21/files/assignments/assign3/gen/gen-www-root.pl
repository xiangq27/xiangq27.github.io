#!/usr/bin/perl -w
use strict;
use warnings;

my $conf_dir = "./conf";
my $doc_root = "./doc-root";

my $conf = "$conf_dir/www-root.conf";

my $DEBUG = 1;

# parse the input file
# the input format of conf.txt is
# <file name>\tprobability of the file

open(MYINPUTFILE, "<$conf");

my $index = 0;
my @files;
my @acc_probs;

# parse conf file
my $acc_prob = 0;
while (<MYINPUTFILE>) {
  my ($line) = $_;
  chomp($line);
  my ($file, $prob) = split(/\t/, $line);

  $acc_prob += $prob;
  $files[$index] = $file;
  $acc_probs[$index] = $acc_prob;

  print "file = $file \t acc_prob = $acc_prob\n" if ($DEBUG);

  $index ++;
}
# force 1
$acc_probs[$index-1] = 1;

my $n = 100;

for (my $i = 1; $i < $n; $i++)
{
  my $r = rand();
	
  print "--Picking $i\n" if ($DEBUG);
  print "  Generate $r\n" if ($DEBUG);
  # find the file to generate
  $index = 0;
  while ($r > $acc_probs[$index]) {
	print "  Checking index $index with $acc_probs[$index]\n" if ($DEBUG);
	$index ++;
  }

  my $file = $files[$index];

  print "  Pick $file\n" if ($DEBUG);
  print "  Command: cp $conf_dir/$file $doc_root/doc$i.html\n" if ($DEBUG);
  `cp $conf_dir/$file $doc_root/doc$i.html`;
}
