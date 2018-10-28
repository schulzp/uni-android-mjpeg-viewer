set size 1,1
set lmargin at screen 0.66

#set key autotitle columnheader
set datafile separator "\t"

set border 0 back linestyle 80
set grid xtics lt 0 lw 1 lc rgb "#CC000000"
set grid ytics lt 0 lw 1 lc rgb "#CC000000"
set tics scale 0
set x2label "agreement"
set xtics ("fully" 1, "↔" 3, "not at all" 5)
set ytics (\
"I think that I would like to use this system frequently." 10,\
"I found the system unnecessarily complex." 9,\
"I thought the system was easy to use." 8,\
"I think that I would need the support of a technical person to be able to use this system." 7,\
"I would imagine that most people would learn to use this system very quickly." 6,\
"I found the system very cumbersome to use." 5,\
"I felt very confident using the system." 4,\
"I think I could identify a deficit in my technique using the system." 3,\
"I think I can improve my technique using the system." 2,\
"I think I will not be able to put in practice what I learned using the system." 1)

set xrange [0:6]
set yrange [0.5:10.5]

set palette defined (1.0 "#000000", 3.0 "#000000", 5.0 "#000000")

set nokey
unset colorbox

set style fill transparent solid 0.5 noborder
#plot for[i=0:9] dataFile every :::0::0 using 1:(10-i):(column(i+2)/20) with circles,\
#                dataFile every :::1::1 using 2:(11-$1):($3) with xerrorbars linecolor rgb "#CC000000"

plot dataFile every :::1::1 using 2:(11-$1):($3) with xerrorbars linecolor rgb "#50000000"
